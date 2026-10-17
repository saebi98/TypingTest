package com.typingtest.korean.data

enum class TypingLanguage(val label: String, val shortLabel: String, val flagEmoji: String) {
    KOREAN("한글 타자", "한글", "🇰🇷"),
    ENGLISH("영문 타자", "영문", "🇺🇸")
}

data class PositionStage(
    val id: Int,
    val title: String,
    val subtitle: String,
    val items: List<String>
)

enum class WordDifficulty(val label: String, val description: String) {
    BEGINNER("초급 단어", "기초 어휘 및 짧은 단어 위주"),
    INTERMEDIATE("중급 단어", "일상 생활 및 필수 어휘"),
    ADVANCED("고급 단어", "고난도 복합 어휘 및 전문 용어")
}

data class LongTextWork(
    val id: String,
    val title: String,
    val author: String,
    val category: String,
    val lines: List<String>
)

object ContentRepository {

    // =========================================================================
    // 1. 자리 연습 (한글 & 영문)
    // =========================================================================

    val koreanPositionStages: List<PositionStage> = listOf(
        PositionStage(
            id = 1,
            title = "1단계: 기본 자리 (중단)",
            subtitle = "ㅁ ㄴ ㅇ ㄹ ㅎ / ㅗ ㅓ ㅏ ㅣ",
            items = listOf(
                "ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ",
                "마", "나", "아", "라", "하", "모", "노", "오", "로", "호",
                "머", "너", "어", "러", "허", "미", "니", "이", "리", "히",
                "안", "란", "한", "만", "온", "론", "혼", "언", "런", "인"
            )
        ),
        PositionStage(
            id = 2,
            title = "2단계: 윗자리 연습 (상단)",
            subtitle = "ㅂ ㅈ ㄷ ㄱ ㅅ / ㅛ ㅕ ㅑ ㅐ ㅔ",
            items = listOf(
                "ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ",
                "바", "자", "다", "가", "사", "보", "조", "도", "고", "소",
                "벼", "져", "뎌", "겨", "셔", "배", "재", "대", "개", "새",
                "베", "제", "데", "게", "세", "산", "강", "달", "밤", "봄"
            )
        ),
        PositionStage(
            id = 3,
            title = "3단계: 아랫자리 연습 (하단)",
            subtitle = "ㅋ ㅌ ㅊ ㅍ / ㅠ ㅜ ㅡ",
            items = listOf(
                "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ",
                "카", "타", "차", "파", "코", "토", "초", "포",
                "쿠", "투", "추", "푸", "크", "트", "츠", "프",
                "구", "두", "무", "부", "수", "주", "우", "유",
                "구름", "우주", "호수", "나무", "하늘", "마음", "웃음"
            )
        ),
        PositionStage(
            id = 4,
            title = "4단계: 쌍자음 및 복합모음·겹받침",
            subtitle = "ㄲ ㄸ ㅃ ㅆ ㅉ / ㅘ ㅝ ㅢ / 겹받침",
            items = listOf(
                "ㄲ", "ㄸ", "ㅃ", "ㅆ", "ㅉ", "ㅒ", "ㅖ",
                "과", "와", "화", "궈", "워", "의", "희", "외", "위",
                "까", "따", "빠", "싸", "짜", "꽃", "꿈", "땅", "별", "빛",
                "닭", "삶", "흙", "값", "몫", "앉", "않", "맑", "밝", "읊"
            )
        )
    )

    val englishPositionStages: List<PositionStage> = listOf(
        PositionStage(
            id = 1,
            title = "Stage 1: 기본 자리 (Home Row)",
            subtitle = "a s d f g / h j k l",
            items = listOf(
                "a", "s", "d", "f", "g", "h", "j", "k", "l",
                "as", "sad", "dad", "lad", "ask", "all", "fall", "hall",
                "glad", "flag", "flak", "half", "dash", "sash", "salad", "glass"
            )
        ),
        PositionStage(
            id = 2,
            title = "Stage 2: 윗자리 연습 (Top Row)",
            subtitle = "q w e r t / y u i o p",
            items = listOf(
                "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                "tree", "water", "power", "quite", "write", "poet", "tower", "route",
                "quote", "pure", "wire", "type", "your", "prior", "report", "quiet"
            )
        ),
        PositionStage(
            id = 3,
            title = "Stage 3: 아랫자리 연습 (Bottom Row)",
            subtitle = "z x c v b / n m",
            items = listOf(
                "z", "x", "c", "v", "b", "n", "m",
                "cab", "van", "man", "ban", "can", "zen", "box", "fox",
                "brave", "vivid", "magic", "novel", "music", "ocean", "cabin", "zebra"
            )
        ),
        PositionStage(
            id = 4,
            title = "Stage 4: 대소문자 혼합 (Shift & Capitals)",
            subtitle = "A~Z 대문자 및 종합 단어",
            items = listOf(
                "A", "S", "D", "F", "J", "K", "L", "Q", "W", "E", "R", "T",
                "Seoul", "Korea", "English", "Android", "Kotlin", "Compose",
                "Dream", "Future", "Nature", "Galaxy", "Victory", "Harmony",
                "Rainbow", "Horizon", "Freedom", "Journey", "Miracle", "Success"
            )
        )
    )

    fun getPositionStages(language: TypingLanguage): List<PositionStage> =
        when (language) {
            TypingLanguage.KOREAN -> koreanPositionStages
            TypingLanguage.ENGLISH -> englishPositionStages
        }

    // =========================================================================
    // 2. 낱말 연습 (한글 & 영문)
    // =========================================================================

    private val koreanBeginnerWords = listOf(
        "하늘", "바다", "구름", "나무", "바람", "소나무", "무지개", "민들레", "진달래", "코스모스",
        "우유", "사과", "포도", "바나나", "수박", "토마토", "옥수수", "고구마", "감자", "오이",
        "기차", "자동차", "비행기", "자전거", "버스", "피아노", "기타", "라디오", "카메라", "시계",
        "학교", "교실", "선생님", "친구", "공책", "연필", "지우개", "가방", "모자", "구두"
    )

    private val koreanIntermediateWords = listOf(
        "대한민국", "무궁화", "한라산", "백두산", "독도", "거북선", "훈민정음", "세종대왕", "이순신", "한글날",
        "도서관", "박물관", "미술관", "운동장", "수영장", "정류장", "지하철", "고속도로", "횡단보도", "신호등",
        "컴퓨터", "스마트폰", "키보드", "모니터", "인터넷", "인공지능", "프로그래밍", "알고리즘", "데이터", "네트워크",
        "행복", "희망", "사랑", "우정", "배려", "존중", "약속", "성실", "열정", "도전"
    )

    private val koreanAdvancedWords = listOf(
        "맑은하늘", "밝은미소", "삶의지혜", "흙냄새", "값진노력", "앉은자리", "끊임없이", "읊조리다", "젊은이", "넓은바다",
        "화기애애", "금상첨화", "다다익선", "동고동락", "대기만성", "일취월장", "유비무환", "온고지신", "청출어람", "설상가상",
        "천고마비", "춘하추동", "사필귀정", "자승자박", "일석이조", "우공이산", "백절불굴", "진인사대천명", "고진감래", "괄목상대",
        "정보통신", "지속가능성", "생태계보전", "기후변화", "신재생에너지", "우주정거장", "양자컴퓨터", "반도체공정", "자율주행", "메타버스"
    )

    private val englishBeginnerWords = listOf(
        "apple", "water", "smile", "cloud", "music", "happy", "river", "green", "bread", "train",
        "house", "light", "world", "heart", "dream", "peace", "ocean", "beach", "stone", "flame",
        "chair", "table", "phone", "watch", "paper", "brush", "plant", "tiger", "eagle", "whale",
        "lemon", "melon", "grape", "peach", "berry", "honey", "sugar", "spoon", "plate", "glass"
    )

    private val englishIntermediateWords = listOf(
        "computer", "keyboard", "internet", "language", "mountain", "sunshine", "adventure", "knowledge", "universe", "calendar",
        "hospital", "library", "museum", "festival", "airplane", "bicycle", "umbrella", "backpack", "notebook", "painting",
        "creative", "positive", "peaceful", "friendly", "grateful", "faithful", "solution", "progress", "memories", "treasure",
        "software", "database", "security", "network", "wireless", "platform", "compiler", "variable", "function", "terminal"
    )

    private val englishAdvancedWords = listOf(
        "algorithm", "architecture", "perseverance", "intelligence", "sustainability", "constellation", "encyclopedia", "collaboration", "transformation", "determination",
        "extraordinary", "philosophical", "unforgettable", "revolutionary", "infrastructure", "biodiversity", "microprocessor", "cryptography", "virtualization", "authentication",
        "consciousness", "magnificent", "serendipity", "entrepreneur", "independence", "productivity", "optimization", "acceleration", "imagination", "achievement",
        "responsibility", "communication", "understanding", "appreciation", "opportunity", "breakthrough", "civilization", "astrophysics", "nanotechnology", "neuroscience"
    )

    fun getWordsByDifficulty(
        difficulty: WordDifficulty,
        language: TypingLanguage = TypingLanguage.KOREAN
    ): List<String> {
        return when (language) {
            TypingLanguage.KOREAN -> when (difficulty) {
                WordDifficulty.BEGINNER -> koreanBeginnerWords.shuffled()
                WordDifficulty.INTERMEDIATE -> koreanIntermediateWords.shuffled()
                WordDifficulty.ADVANCED -> koreanAdvancedWords.shuffled()
            }
            TypingLanguage.ENGLISH -> when (difficulty) {
                WordDifficulty.BEGINNER -> englishBeginnerWords.shuffled()
                WordDifficulty.INTERMEDIATE -> englishIntermediateWords.shuffled()
                WordDifficulty.ADVANCED -> englishAdvancedWords.shuffled()
            }
        }
    }

    // =========================================================================
    // 3. 짧은 글 연습 (한글 & 영문)
    // =========================================================================

    val koreanShortSentences: List<String> = listOf(
        "가는 말이 고와야 오는 말이 곱다.",
        "천 리 길도 한 걸음부터 시작된다.",
        "시작이 반이라는 말을 믿고 나아가자.",
        "낮말은 새가 듣고 밤말은 쥐가 듣는다.",
        "티끌 모아 태산이 된다.",
        "고생 끝에 낙이 온다.",
        "금강산도 식후경이다.",
        "백지장도 맞들면 낫다.",
        "벼는 익을수록 고개를 숙인다.",
        "비 온 뒤에 땅이 굳어진다.",
        "세 살 버릇 여든까지 간다.",
        "소 잃고 외양간 고친다.",
        "원숭이도 나무에서 떨어질 때가 있다.",
        "우물 안 개구리가 되지 말자.",
        "작은 고추가 더 맵다.",
        "콩 심은 데 콩 나고 팥 심은 데 팥 난다.",
        "하늘은 스스로 돕는 자를 돕는다.",
        "호랑이에게 물려가도 정신만 차리면 산다.",
        "오늘 걷지 않으면 내일은 뛰어야 한다.",
        "실패는 성공의 어머니이다.",
        "독서는 마음의 양식이다.",
        "웃음은 만병통치약과 같다.",
        "시간은 금보다 귀중한 자산이다.",
        "가장 어두운 밤도 결국 끝나고 해는 떠오른다.",
        "작은 습관이 모여 큰 운명을 만든다.",
        "어제보다 나은 오늘을 위해 꾸준히 연습하자.",
        "한글은 세계에서 가장 과학적인 문자입니다.",
        "맑은 바람이 불어오는 가을 하늘이 참 높습니다.",
        "따뜻한 차 한 잔과 함께 여유로운 하루를 보내세요.",
        "정확하게 치는 습관이 결국 가장 빠른 타자를 만듭니다."
    )

    val englishShortSentences: List<String> = listOf(
        "The quick brown fox jumps over the lazy dog.",
        "Actions speak louder than words.",
        "Practice makes perfect in every skill.",
        "Where there is a will, there is a way.",
        "A journey of a thousand miles begins with a single step.",
        "The early bird catches the worm.",
        "Knowledge is power and kindness is strength.",
        "Rome was not built in a day.",
        "Every cloud has a silver lining.",
        "Honesty is the best policy in life.",
        "Time waits for no one, so treasure today.",
        "Fall seven times and stand up eight.",
        "Success is the sum of small efforts repeated daily.",
        "Stay hungry, stay foolish, and keep learning.",
        "Believe you can and you are halfway there.",
        "Simplicity is the ultimate sophistication.",
        "To be or not to be, that is the question.",
        "All that glitters is not gold.",
        "Better late than never, but never late is better.",
        "Fortune favors the bold and prepared mind.",
        "Reading is to the mind what exercise is to the body.",
        "You never know what you can do until you try.",
        "Great oaks from little acorns grow.",
        "No pain, no gain on the road to mastery.",
        "Accuracy always comes before typing speed.",
        "Look before you leap into a new challenge.",
        "A friend in need is a friend indeed.",
        "Laughter is the shortest distance between two people.",
        "Stars cannot shine without darkness.",
        "Make each day your masterpiece."
    )

    fun getShortSentences(language: TypingLanguage): List<String> =
        when (language) {
            TypingLanguage.KOREAN -> koreanShortSentences
            TypingLanguage.ENGLISH -> englishShortSentences
        }

    // =========================================================================
    // 4. 긴 글 연습 (한글 & 영문)
    // =========================================================================

    val koreanLongTextWorks: List<LongTextWork> = listOf(
        LongTextWork(
            id = "star_night",
            title = "별 헤는 밤",
            author = "윤동주",
            category = "한국 현대시",
            lines = listOf(
                "계절이 지나가는 하늘에는 가을로 가득 차 있습니다.",
                "나는 아무 걱정도 없이 가을 속의 별들을 다 헤일 듯합니다.",
                "가슴 속에 하나 둘 새겨지는 별을 이제 다 못 헤는 것은",
                "쉬이 아침이 오는 까닭이요, 내일 밤이 남은 까닭이요,",
                "아직 나의 청춘이 다하지 않은 까닭입니다.",
                "별 하나에 추억과 별 하나에 사랑과",
                "별 하나에 쓸쓸함과 별 하나에 동경과",
                "별 하나에 시와 별 하나에 어머니, 어머니,",
                "어머님, 나는 별 하나에 아름다운 말 한마디씩 불러 봅니다.",
                "소학교 때 책상을 같이 했던 아이들의 이름과,",
                "패, 경, 옥, 이런 이국 소녀들의 이름과,",
                "벌써 아기 어머니 된 계집애들의 이름과,",
                "가난한 이웃 사람들의 이름과,",
                "비둘기, 강아지, 토끼, 노새, 노루, 프랑시스 잠,",
                "라이너 마리아 릴케, 이런 시인의 이름을 불러 봅니다.",
                "이네들은 너무나 멀리 있습니다. 별이 아스라이 멀듯이.",
                "어머님, 그리고 당신은 멀리 북간도에 계십니다.",
                "나는 무엇인지 그리워 이 많은 별빛이 내린 언덕 위에",
                "내 이름자를 써 보고 흙으로 덮어 버리었습니다.",
                "딴은 밤을 세워 우는 벌레는 부끄러운 이름을 슬퍼하는 까닭입니다.",
                "그러나 겨울이 지나고 나의 별에도 봄이 오면",
                "무덤 위에 파란 잔디가 피어나듯이",
                "내 이름자 묻힌 언덕 위에도 자랑처럼 풀이 무성할 거외다."
            )
        ),
        LongTextWork(
            id = "sonagi",
            title = "소나기",
            author = "황순원",
            category = "한국 단편소설",
            lines = listOf(
                "소년은 개울가에서 소녀를 보자 곧 윤 초시네 증손녀딸이라는 걸 알 수 있었다.",
                "소녀는 개울에다 손을 잠그고 물장난을 하고 있는 것이다.",
                "서울서는 이런 개울물을 보지 못하기나 한 듯이.",
                "벌써 며칠째 소녀는 학교에서 돌아오는 길에 물장난을 하였다.",
                "그런데 어제까지 개울 기슭에서 하더니 오늘은 징검다리 한가운데 앉아서 하고 있다.",
                "소년은 개울둑에 앉아 버렸다. 소녀가 비키기를 기다리자는 것이다.",
                "요행 지나가는 사람이 있어 소녀가 길을 비켜 주었다.",
                "다음 날은 좀 늦게 개울가로 나왔다.",
                "이날은 소녀가 징검다리 한가운데 앉아 세수를 하고 있었다.",
                "분홍 스웨터 소매를 걷어올린 목덜미가 마냥 희었다.",
                "한참 세수를 하고 나더니 이번에는 물 속을 쩌르르 들여다보았다.",
                "얼굴이라도 비추어 보는 것이리라. 갑자기 물을 움켜낸다.",
                "고기 새끼라도 지나가는 듯.",
                "소녀는 소년이 개울둑에 앉아 있는 걸 아는지 모르는지 그냥 날쌔게 물만 움켜낸다.",
                "그러나 번번이 허탕이다. 그대로 재미있는 양, 자꾸 물만 움킨다.",
                "어제처럼 개울을 건너가는 사람이 있어야 길을 비킬 모양이다.",
                "그러다가 소녀가 물 속에서 무엇을 하나 집어낸다. 하얀 조약돌이었다.",
                "그리고는 벌떡 일어나 팔총팔총 징검다리를 뛰어 건너간다.",
                "다 건너가더니만 휙 이리로 돌아서며, 이 바보, 조약돌이 날아왔다.",
                "소년은 자기도 모르게 벌떡 일어섰다."
            )
        ),
        LongTextWork(
            id = "youth",
            title = "청춘예찬",
            author = "민태원",
            category = "한국 현대수필",
            lines = listOf(
                "청춘! 이는 듣기만 하여도 가슴이 설레는 말이다.",
                "청춘! 너의 두 손을 가슴에 대고, 물방아 같은 심장의 고동을 들어 보라.",
                "청춘의 피는 끓는다. 끓는 피에 뛰노는 심장은 거선의 기관과 같이 힘있다.",
                "이것이다. 인류의 역사를 꾸며 내려온 동력은 바로 이것이다.",
                "이성은 투명하되 얼음과 같으며, 지혜는 날카로우나 갑 속에 든 칼이다.",
                "청춘의 끓는 피가 아니더면, 인간이 얼마나 쓸쓸하랴?",
                "얼음에 싸인 만물은 죽음이 있을 뿐이다.",
                "그들에게 생명을 불어넣는 것은 따뜻한 봄바람이다.",
                "풀밭에 속잎나고, 가지에 싹이 트고, 꽃 피고 새 우는 봄날의 천지는 얼마나 기쁘며 얼마나 아름다우냐?",
                "이것을 얼음 속에서 불러내는 것이 따뜻한 봄바람이다.",
                "인생에 따뜻한 봄바람을 불어 보내는 것은 청춘의 끓는 피다.",
                "청춘의 피가 뜨거운지라, 인간의 동산에는 사랑의 풀이 돋고, 이상의 꽃이 피고,",
                "희망의 놀이 뜨고, 열락의 새가 운다."
            )
        ),
        LongTextWork(
            id = "buckwheat",
            title = "메밀꽃 필 무렵",
            author = "이효석",
            category = "한국 단편소설",
            lines = listOf(
                "여름장이란 애시당초에 글러서, 해는 아직 중천에 있건만 장판은 벌써 쓸쓸하고 더운 햇발이 벌여 놓았던 전 휘장 밑으로 등줄기를 훅훅 볶는다.",
                "마을 사람들은 거지반 돌아간 뒤요, 팔리지 못한 나뭇군패가 길거리에 궁싯거리고들 있으나,",
                "석유병이나 받고 고깃마리나 사면 족할 이 축들을 바라고 언제까지든지 버티고 있을 법은 없다.",
                "이지러는 졌으나 보름을 갓 지난 달은 부드러운 빛을 흐뭇이 흘리고 있다.",
                "대화까지는 팔십 리의 밤길, 고개를 둘이나 넘고 개울을 하나 건너고 벌판과 산길을 걸어야 된다.",
                "길은 지금 긴 산허리에 걸려 있다. 밤중을 지난 무렵인지 죽은 듯이 고요한 속에서 짐승 같은 달의 숨소리가 손에 잡힐 듯이 들리며,",
                "콩포기와 옥수수 잎새가 한층 달에 푸르게 젖었다.",
                "산허리는 온통 메밀밭이어서 피기 시작한 꽃이 소금을 뿌린 듯이 흐뭇한 달빛에 숨이 막힐 지경이다.",
                "붉은 대궁이 향기같이 애잔하고 나귀들의 걸음도 시원하다."
            )
        ),
        LongTextWork(
            id = "azalea",
            title = "진달래꽃 & 엄마야 누나야",
            author = "김소월",
            category = "한국 현대시",
            lines = listOf(
                "나 보기가 역겨워 가실 때에는 말없이 고이 보내 드리우리다.",
                "영변에 약산 진달래꽃 아름 따다 가실 길에 뿌리오리다.",
                "가시는 걸음걸음 놓인 그 꽃을 사뿐히 즈려밟고 가시옵소서.",
                "나 보기가 역겨워 가실 때에는 죽어도 아니 눈물 흘리우리다.",
                "엄마야 누나야 강변 살자. 뜰에는 반짝이는 금모래빛,",
                "뒷문 밖에는 갈잎의 노래, 엄마야 누나야 강변 살자.",
                "산에는 꽃 피네 꽃이 피네 갈 봄 여름 없이 꽃이 피네.",
                "산에 산에 피는 꽃은 저만치 혼자서 피어 있네."
            )
        )
    )

    val englishLongTextWorks: List<LongTextWork> = listOf(
        LongTextWork(
            id = "road_not_taken",
            title = "The Road Not Taken",
            author = "Robert Frost",
            category = "Classic Poetry",
            lines = listOf(
                "Two roads diverged in a yellow wood,",
                "And sorry I could not travel both",
                "And be one traveler, long I stood",
                "And looked down one as far as I could",
                "To where it bent in the undergrowth;",
                "Then took the other, as just as fair,",
                "And having perhaps the better claim,",
                "Because it was grassy and wanted wear;",
                "Though as for that the passing there",
                "Had worn them really about the same,",
                "And both that morning equally lay",
                "In leaves no step had trodden black.",
                "Oh, I kept the first for another day!",
                "Yet knowing how way leads on to way,",
                "I doubted if I should ever come back.",
                "I shall be telling this with a sigh",
                "Somewhere ages and ages hence:",
                "Two roads diverged in a wood, and I,",
                "I took the one less traveled by,",
                "And that has made all the difference."
            )
        ),
        LongTextWork(
            id = "gettysburg",
            title = "The Gettysburg Address",
            author = "Abraham Lincoln",
            category = "Historic Speech",
            lines = listOf(
                "Four score and seven years ago our fathers brought forth on this continent, a new nation,",
                "conceived in Liberty, and dedicated to the proposition that all men are created equal.",
                "Now we are engaged in a great civil war, testing whether that nation,",
                "or any nation so conceived and so dedicated, can long endure.",
                "We are met on a great battle-field of that war.",
                "We have come to dedicate a portion of that field, as a final resting place",
                "for those who here gave their lives that that nation might live.",
                "It is altogether fitting and proper that we should do this.",
                "But, in a larger sense, we can not dedicate, we can not consecrate, we can not hallow this ground.",
                "The brave men, living and dead, who struggled here, have consecrated it,",
                "far above our poor power to add or detract.",
                "The world will little note, nor long remember what we say here,",
                "but it can never forget what they did here.",
                "It is for us the living, rather, to be dedicated here to the unfinished work",
                "which they who fought here have thus far so nobly advanced.",
                "that this nation, under God, shall have a new birth of freedom,",
                "and that government of the people, by the people, for the people, shall not perish from the earth."
            )
        ),
        LongTextWork(
            id = "sonnet_18",
            title = "Sonnet 18",
            author = "William Shakespeare",
            category = "Classic Poetry",
            lines = listOf(
                "Shall I compare thee to a summer's day?",
                "Thou art more lovely and more temperate:",
                "Rough winds do shake the darling buds of May,",
                "And summer's lease hath all too short a date:",
                "Sometime too hot the eye of heaven shines,",
                "And often is his gold complexion dimmed;",
                "And every fair from fair sometime declines,",
                "By chance, or nature's changing course, untrimmed;",
                "But thy eternal summer shall not fade,",
                "Nor lose possession of that fair thou owest;",
                "Nor shall Death brag thou wanderest in his shade,",
                "When in eternal lines to time thou growest:",
                "So long as men can breathe, or eyes can see,",
                "So long lives this, and this gives life to thee."
            )
        ),
        LongTextWork(
            id = "hope_feathers",
            title = "Hope is the Thing with Feathers",
            author = "Emily Dickinson",
            category = "Classic Poetry",
            lines = listOf(
                "Hope is the thing with feathers",
                "That perches in the soul,",
                "And sings the tune without the words,",
                "And never stops at all,",
                "And sweetest in the gale is heard;",
                "And sore must be the storm",
                "That could abash the little bird",
                "That kept so many warm.",
                "I have heard it in the chillest land,",
                "And on the strangest sea;",
                "Yet, never, in extremity,",
                "It asked a crumb of me."
            )
        ),
        LongTextWork(
            id = "if_kipling",
            title = "If (Excerpt)",
            author = "Rudyard Kipling",
            category = "Classic Poetry",
            lines = listOf(
                "If you can keep your head when all about you",
                "Are losing theirs and blaming it on you,",
                "If you can trust yourself when all men doubt you,",
                "But make allowance for their doubting too;",
                "If you can wait and not be tired by waiting,",
                "Or being lied about, do not deal in lies,",
                "Or being hated, do not give way to hating,",
                "And yet do not look too good, nor talk too wise:",
                "If you can dream and not make dreams your master;",
                "If you can think and not make thoughts your aim;",
                "If you can meet with Triumph and Disaster",
                "And treat those two impostors just the same;",
                "Yours is the Earth and everything that is in it."
            )
        )
    )

    fun getLongTextWorks(language: TypingLanguage): List<LongTextWork> =
        when (language) {
            TypingLanguage.KOREAN -> koreanLongTextWorks
            TypingLanguage.ENGLISH -> englishLongTextWorks
        }

    // =========================================================================
    // 5. 산성비 단어 풀 (한글 & 영문)
    // =========================================================================

    private val koreanAcidRainPool: List<String> = (koreanBeginnerWords + koreanIntermediateWords + listOf(
        "은하수", "별똥별", "인공위성", "우주선", "북극성", "오리온", "안드로메다",
        "소나기", "번개", "천둥", "눈보라", "아지랑이", "새벽별", "노을빛",
        "비빔밥", "불고기", "김치찌개", "된장찌개", "떡볶이", "삼계탕", "갈비탕",
        "거북이", "다람쥐", "산토끼", "부엉이", "독수리", "돌고래", "호랑이",
        "도전정신", "백절불굴", "일취월장", "금상첨화", "유비무환", "청출어람"
    )).distinct()

    private val englishAcidRainPool: List<String> = (englishBeginnerWords + englishIntermediateWords + listOf(
        "galaxy", "meteor", "planet", "rocket", "nebula", "cosmos", "horizon",
        "thunder", "tornado", "rainbow", "glacier", "volcano", "breeze", "sunrise",
        "dolphin", "panther", "falcon", "penguin", "leopard", "cheetah", "gazelle",
        "courage", "victory", "harmony", "freedom", "justice", "passion", "destiny"
    )).distinct()

    fun getAcidRainWordPool(language: TypingLanguage): List<String> =
        when (language) {
            TypingLanguage.KOREAN -> koreanAcidRainPool
            TypingLanguage.ENGLISH -> englishAcidRainPool
        }
}
