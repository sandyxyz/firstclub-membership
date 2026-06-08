param(
    [ValidateSet("compile", "test", "run")]
    [string] $Task = "test"
)

$ErrorActionPreference = "Stop"
$repo = Join-Path $env:USERPROFILE ".m2\repository"
$excludedArtifacts = @(
    "spring-boot-starter-jdbc",
    "spring-boot-starter-data-redis",
    "spring-jdbc",
    "spring-data-commons",
    "spring-data-keyvalue",
    "spring-data-redis",
    "HikariCP",
    "lettuce-core",
    "jedis"
)
$jars = Get-ChildItem $repo -Recurse -Filter "*.jar" |
    Where-Object { $excludedArtifacts -notcontains $_.Directory.Parent.Name } |
    Group-Object { $_.Directory.Parent.FullName } |
    ForEach-Object {
        $_.Group |
            Sort-Object @{
                Expression = {
                    try { [version]$_.Directory.Name } catch { [version]"0.0.0" }
                }
            }, @{ Expression = { $_.Directory.Name } } -Descending |
            Select-Object -First 1
    } |
    ForEach-Object { $_.FullName }
$classpath = [string]::Join([IO.Path]::PathSeparator, $jars)
$mainOut = "target\classes"
$testOut = "target\test-classes"
New-Item -ItemType Directory -Force -Path $mainOut, $testOut | Out-Null
if (Test-Path "src\main\resources") {
    Copy-Item "src\main\resources\*" $mainOut -Recurse -Force
}

$mainSources = Get-ChildItem "src\main\java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
if ($mainSources.Count -gt 0) {
    javac -encoding UTF-8 -parameters -cp $classpath -d $mainOut @mainSources
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

if ($Task -eq "compile") {
    exit 0
}

if ($Task -eq "run") {
    $runClasspath = [string]::Join([IO.Path]::PathSeparator, @($mainOut, $classpath))
    java -cp $runClasspath com.firstclub.membership.MembershipProgramApplication
    exit 0
}

$testSources = Get-ChildItem "src\test\java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
if ($testSources.Count -gt 0) {
    $testClasspath = [string]::Join([IO.Path]::PathSeparator, @($mainOut, $classpath))
    javac -encoding UTF-8 -parameters -cp $testClasspath -d $testOut @testSources
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    $runClasspath = [string]::Join([IO.Path]::PathSeparator, @($mainOut, $testOut, $classpath))
    java -cp $runClasspath com.firstclub.membership.TestRunner
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
