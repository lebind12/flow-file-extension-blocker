# PowerShell 동시성 테스트 스크립트
# 사용법: .\concurrency_test.ps1

$baseUrl = "http://localhost:8080/api/extensions/custom"
$extension = "concurrent"
$threadCount = 10

Write-Host "=== 동시성 테스트 시작 ===" -ForegroundColor Cyan
Write-Host "동시 요청 수: $threadCount"
Write-Host "테스트 확장자: $extension"
Write-Host ""

# 기존 테스트 데이터 삭제 (있다면)
try {
    Invoke-RestMethod -Uri "$baseUrl/$extension" -Method DELETE -ErrorAction SilentlyContinue
} catch {}

# 동시 요청 실행
$jobs = @()
for ($i = 1; $i -le $threadCount; $i++) {
    $jobs += Start-Job -ScriptBlock {
        param($url, $ext)
        try {
            $body = @{ extension = $ext } | ConvertTo-Json
            $response = Invoke-RestMethod -Uri $url -Method POST -Body $body -ContentType "application/json" -ErrorAction Stop
            return @{ success = $true; thread = $using:i }
        } catch {
            $errorMsg = $_.ErrorDetails.Message | ConvertFrom-Json -ErrorAction SilentlyContinue
            return @{ success = $false; thread = $using:i; error = $errorMsg.message }
        }
    } -ArgumentList $baseUrl, $extension
}

# 결과 수집
Write-Host "요청 완료 대기 중..." -ForegroundColor Yellow
$results = $jobs | Wait-Job | Receive-Job
$jobs | Remove-Job

# 결과 분석
$successCount = ($results | Where-Object { $_.success -eq $true }).Count
$failCount = ($results | Where-Object { $_.success -eq $false }).Count

Write-Host ""
Write-Host "=== 테스트 결과 ===" -ForegroundColor Green
Write-Host "성공: $successCount" -ForegroundColor Green
Write-Host "실패: $failCount" -ForegroundColor Red

if ($successCount -eq 1 -and $failCount -eq ($threadCount - 1)) {
    Write-Host ""
    Write-Host "[PASS] 정확히 1개만 성공, 나머지는 중복 에러" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[WARN] 예상과 다른 결과" -ForegroundColor Yellow
}

# 실패 메시지 출력
$errors = $results | Where-Object { $_.success -eq $false } | Select-Object -First 1
if ($errors) {
    Write-Host "에러 메시지: $($errors.error)" -ForegroundColor Gray
}

# 정리
try {
    Invoke-RestMethod -Uri "$baseUrl/$extension" -Method DELETE -ErrorAction SilentlyContinue
} catch {}

Write-Host ""
Write-Host "테스트 완료" -ForegroundColor Cyan
