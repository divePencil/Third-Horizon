$env:npm_config_offline = "false"
$env:HTTP_PROXY = ""
$env:HTTPS_PROXY = ""
$env:http_proxy = ""
$env:https_proxy = ""
$env:ALL_PROXY = ""
$env:all_proxy = ""

npm install --omit=optional --cache "$PSScriptRoot\.npm-cache"
npm run dev
