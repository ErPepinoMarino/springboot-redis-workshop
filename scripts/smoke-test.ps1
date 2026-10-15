$ErrorActionPreference = "Stop"
$backend  = "http://localhost:8000"
$frontend = "http://localhost:3000"
$failures = 0

function Wait-Healthy($url, $seconds) {
  $deadline = (Get-Date).AddSeconds($seconds)
  while ((Get-Date) -lt $deadline) {
    try {
      $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
      if ($r.StatusCode -eq 200) { return $true }
    } catch { }
    Start-Sleep -Seconds 2
  }
  return $false
}

function Check($name, $url, $expected, $method = "GET") {
  try {
    $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -Method $method
    $status = [int]$r.StatusCode
  } catch {
    $status = [int]$_.Exception.Response.StatusCode.value__
  }
  if ($status -eq $expected) {
    Write-Host ("  OK    {0}  ({1})" -f $name, $status) -ForegroundColor Green
  } else {
    Write-Host ("  FAIL  {0}  (esperado {1}, recibido {2})" -f $name, $expected, $status) -ForegroundColor Red
    $script:failures++
  }
}

Write-Host "Smoke test - Spring Boot / Redis Workshop" -ForegroundColor Cyan

if (-not (Wait-Healthy "$backend/healthz" 90)) {
  Write-Host "El backend no respondio en 90s. Levantaste 'docker compose up'?" -ForegroundColor Red
  exit 1
}

Check "backend   GET /healthz"                     "$backend/healthz" 200
Check "cache     GET /cache/products/1"            "$backend/cache/products/1" 200
Check "cache     GET /cache/products/1?cached=false" "$backend/cache/products/1?cached=false" 200
Check "ratelimit GET /ratelimit/ping"              "$backend/ratelimit/ping" 200
Check "ratelimit GET /ratelimit/simulate"          "$backend/ratelimit/simulate" 200
Check "sessions  GET /sessions/me (sin cookie -> 401)" "$backend/sessions/me" 401
Check "clock     GET /clock"                       "$backend/clock" 200
Check "jobs      POST /jobs"                       "$backend/jobs" 200 "POST"
Check "lock      POST /lock/acquire?client=smoke"  "$backend/lock/acquire?client=smoke" 200 "POST"
Check "lock      POST /lock/release?client=smoke"  "$backend/lock/release?client=smoke" 200 "POST"
Check "frontend  GET /"                            "$frontend/" 200

if ($failures -eq 0) {
  Write-Host "`nTODO OK" -ForegroundColor Green
  exit 0
} else {
  Write-Host "`n$failures comprobacion(es) fallida(s)" -ForegroundColor Red
  exit 1
}