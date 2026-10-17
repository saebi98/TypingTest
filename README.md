# 한글 타자 마스터 (Korean Typing Master) - Android App

스마트폰 터치 키보드와 블루투스 물리 키보드를 모두 지원하는 Android Native (**Kotlin + Jetpack Compose**) 기반 한글 타자 연습 애플리케이션입니다.

## 📦 빌드된 설치 파일 (APK)
- **프로젝트 루트 바로가기**: `c:\Data\Github\TypingTest\KoreanTypingMaster.apk` (약 10.5 MB, 서명 완료되어 안드로이드 기기에 바로 설치 가능)
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`

## ✨ 주요 기능
1. **한글 전용 타자 엔진 (`HangulEngine`)**
   - 초·중·종성 및 복모음(`ㅘ`, `ㅝ`, `ㅢ` 등)/겹받침(`ㄺ`, `ㄻ`, `ㅄ` 등) 키 입력 타수(Stroke) 단위 정밀 분해
   - 한글 IME 조합 중 상태(`한` 입력 중 `ㅎ` → `하`) 및 도깨비불 연음 현상(`바다` 입력 중 `받`)을 오타가 아닌 **정상 조합 중(Composing)** 으로 실시간 판정
   - 내장 두벌식 가상 키보드용 한글 오토마타(합성 및 백스페이스 역분해) 탑재
2. **4대 단계별 연습 모드**
   - **자리 연습**: 기본 자리(중단) → 윗자리(상단) → 아랫자리(하단) → 쌍자음·복합모음·겹받침 (다음 눌러야 할 자모 및 손가락 위치 하이라이트 제공)
   - **낱말 연습**: 초급 / 중급 / 고급 120여 개 한국어 어휘 연습
   - **짧은 글 연습**: 한국 속담, 명언, 아름다운 우리말 단문 타이핑
   - **긴 글 연습**: 윤동주 「별 헤는 밤」, 황순원 「소나기」, 민태원 「청춘예찬」, 이효석 「메밀꽃 필 무렵」, 김소월 「진달래꽃」 페이지 단위 연습
3. **산성비 타자 디펜스 게임 (`AcidRainGameScreen`)**
   - 실시간 단어 낙하 디펜스 게임 (입력 일치 시 즉시 자동 격추)
   - 특수 아이템 단어: 🧹전체 청소(화면 내 단어 모두 제거), ❄️감속(6초간 낙하 속도 50% 감소), ❤️회복(생명력 +1 회복)
4. **내 타자 통계 & 히스토리 (`TypingRecordStore` + `StatisticsScreen`)**
   - SQLite 로컬 데이터베이스 기반 모드별 평균/최고 타수, 정확도, 산성비 최고 점수, 최근 타수 추이 라인 차트 제공

## 🛠️ 빌드 및 테스트 방법
```powershell
# 단위 테스트 실행
.\gradlew.bat testDebugUnitTest

# Debug 및 Release APK 빌드
.\gradlew.bat assembleDebug assembleRelease
```
