# 한/영 타자 마스터 (Korean & English Typing Master) - Android App

스마트폰 터치 키보드와 블루투스 물리 키보드를 모두 지원하며, **한글(두벌식) 및 영문(QWERTY) 타자 연습**을 모두 제공하는 Android Native (**Kotlin + Jetpack Compose**) 애플리케이션입니다.

## 📦 빌드된 설치 파일 (APK)
- **프로젝트 루트 바로가기**: `c:\Data\Github\TypingTest\KoreanTypingMaster.apk` (서명 완료되어 안드로이드 기기에 바로 설치 가능)
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`

## ✨ 주요 기능
1. **🇰🇷 한글 & 🇺🇸 영문 통합 타자 엔진 (`HangulEngine`)**
   - **한글**: 초·중·종성 및 복모음(`ㅘ`, `ㅝ`, `ㅢ` 등)/겹받침(`ㄺ`, `ㄻ`, `ㅄ` 등) 키 입력 타수(Stroke) 단위 분해, 한글 IME 조합 중 상태 및 도깨비불 연음 현상 실시간 판정
   - **영문**: 대소문자(`Shift + A~Z`) 및 문장부호 감지, 영문 QWERTY 기준 다음 입력 키 및 추천 손가락 안내
   - **내장 가상 키보드 (`DubeolsikKeyboard`)**: 한글 두벌식 자판과 영문 QWERTY 자판 가이드를 즉시 전환하며 터치 입력 및 물리 키보드 가이드 지원
2. **4대 단계별 연습 모드 (한글 / 영문 즉시 전환)**
   - **자리 연습**:
     - 한글: 기본 자리(중단) → 윗자리(상단) → 아랫자리(하단) → 쌍자음·복합모음·겹받침
     - 영문: Home Row (`a~l`) → Top Row (`q~p`) → Bottom Row (`z~m`) → Shift & 대소문자 혼합
   - **낱말 연습**: 초급 / 중급 / 고급 한국어 어휘 120선 & 필수 영단어 120선
   - **짧은 글 연습**: 한국 속담·명언 & 영어 속담·격언(`The quick brown fox...`, `Actions speak louder than words.` 등)
   - **긴 글 연습**:
     - 한국 명작 문학 5선: 윤동주 「별 헤는 밤」, 황순원 「소나기」, 민태원 「청춘예찬」, 이효석 「메밀꽃 필 무렵」, 김소월 「진달래꽃」
     - 세계 영문 명작 5선: Robert Frost *"The Road Not Taken"*, Abraham Lincoln *"The Gettysburg Address"*, William Shakespeare *"Sonnet 18"*, Emily Dickinson *"Hope is the Thing with Feathers"*, Rudyard Kipling *"If"*
3. **산성비 타자 디펜스 게임 (`AcidRainGameScreen`)**
   - 한글 단어 모드 / 영단어 모드 즉시 전환 지원
   - 특수 아이템 단어: 🧹전체 청소(화면 내 단어 모두 제거), ❄️감속(6초간 낙하 속도 50% 감소), ❤️회복(생명력 +1 회복)
4. **내 타자 통계 & 히스토리 (`TypingRecordStore` + `StatisticsScreen`)**
   - SQLite 로컬 데이터베이스 기반 한/영 모드별 평균/최고 타수, 정확도, 산성비 최고 점수, 최근 타수 추이 라인 차트 제공

## 🛠️ 빌드 및 테스트 방법
```powershell
# 단위 테스트 실행
.\gradlew.bat testDebugUnitTest

# Debug 및 Release APK 빌드
.\gradlew.bat assembleDebug assembleRelease
```
