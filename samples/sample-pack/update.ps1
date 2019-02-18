 param (
    [string]$app = "sample",
    [Parameter(Mandatory=$true)][string]$server,
    [Parameter(Mandatory=$true)][string]$port,
    [switch]$install = $false,
    [switch]$wait = $false,
    [switch]$help = $false
)

[string]$Dest = $($HOME) + "\" + $($app) + "-runtime";
write-output "Dest=$($Dest)";

[string]$ScriptDir = Split-Path $script:MyInvocation.MyCommand.Path
 
Add-Type -AssemblyName System.IO.Compression.FileSystem
#Add-Type -Path C:\Windows\Microsoft.NET\assembly\GAC_MSIL\System.IO.Compression.FileSystem\v4.0_4.0.0.0__b77a5c561934e089\System.IO.Compression.FileSystem.dll

function usage
{
    write-output ""
    write-output "usage:"
    write-output "update.ps1 -server <server> -port <port> [-install] [-wait] [-h]"
    Exit
}

if ( $help ) {
    usage
}

function Unzip
{
    param([string]$zipfile, [string]$outpath)
    $t = Test-Path $outpath;
    if ( $t ) {
        rm -r -fo $($outpath);
     }
#    [System.IO.Compression.ZipFile]::ExtractToDirectory($zipfile, $outpath)
}

function Update
{
[string]$BaseUrl = "http://" + $($server) + ":" + $($port) + "/" + $($app) + "/bootstrap/windows";
write-output "BaseURL=$($BaseUrl)";


[string]$ScriptDir = Split-Path $script:MyInvocation.MyCommand.Path

[string]$url=$($BaseUrl)+"/"+"update.sha256";
[string]$output=$($ScriptDir)+"\"+"update.sha256";
 
$wc = New-Object System.Net.WebClient
write-output "downloading $($url) to $($output)";
 
 Try {
 $wc.DownloadFile($($url), $($output))
} Catch {
    write-output "Error downloading file $($output)";
    Exit
}

$sha = Get-Content -Path $output

$mustUpdate = $true;

$Check="$($Dest)\update.sha256";

$e=Test-Path $Check;

if ( $e ) {
    write-output "Checking ${$Check$}"
    $sha2 = Get-Content -Path $Check
    if ( $($sha2) -eq $($sha) ) {
        $mustUpdate=$false;
    }
}


if ( $mustUpdate ) {

    write-output "Runtime Update required"

[string]$url=$($BaseUrl)+"/"+"update.zip";
[string]$output2=$($ScriptDir)+"\"+"update.zip";

write-output "downloading $($url) to $($output2)";
Try {
 $wc.DownloadFile($($url), $($output2))
} Catch {
    write-output "Error downloading file $($output2)";
    Exit
}

$hashFromFile = Get-FileHash $($output2)

if ( $hashFromFile.hash -ne $sha ) {
    write-output "hash values differ";
    write-output "expected: $sha";
    write-output "     got: $($hashFromFile.hash)";
    Exit
} else {
    write-output "SHA Hash Values matches: $sha"; 
}

Unzip $($output2) $($Dest)

cp $($output) $($Check)

} else {

    write-output "No Runtime Update required"

}
}

function Install
{
[string]$ScriptName=$script:MyInvocation.MyCommand.Name
[string]$Starter="$($ScriptDir)\${ScriptName}";
write-output "Starter=$($Starter)"
	
$Shell = New-Object -ComObject ("WScript.Shell")
$ShortCut = $Shell.CreateShortcut($env:USERPROFILE + "\Desktop\$($app).lnk")
$ShortCut.TargetPath="%SystemRoot%\system32\WindowsPowerShell\v1.0\powershell.exe"
$ShortCut.Arguments="-WindowStyle Hidden -ExecutionPolicy Bypass -File $($ScriptDir)\$($ScriptName) `"-server`" `"$($server)`" `"-port`" `"$($port)`"";
$ShortCut.WorkingDirectory = "$($Dest2)";
$ShortCut.WindowStyle = 1;
$ShortCut.IconLocation = "$($ScriptDir)\icon.ico";
#$ShortCut.Description = "Your Custom Shortcut Description";
$ShortCut.Save()
}


function Launch
{
    cd $($Dest)
    
    Start-Process -FilePath bin\java -ArgumentList "--module-path app\jar_auto -splash:conf\splash.gif net.agilhard.jpacktool.util.update4j.JPacktoolBootstrap  $args" -WindowStyle Hidden -PassThru

}


if ( $install ) {
Install
} else {
Update
Launch
}


if ( $wait ) {
$( Read-Host "Type anything to quit" );
}


