$Host.UI.RawUI.WindowTitle = "Museum Backend"
$securePassword = Read-Host "Input MySQL password" -AsSecureString
$credential = New-Object System.Management.Automation.PSCredential("root", $securePassword)
$env:DB_HOST = "127.0.0.1"
$env:DB_PORT = "3306"
$env:DB_NAME = "museum_reservation"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = $credential.GetNetworkCredential().Password
java -jar target\museum-reservation-backend-0.0.1-SNAPSHOT.jar
