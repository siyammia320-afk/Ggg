package com.example.data

enum class Country(
    val code: String,
    val displayName: String,
    val flagEmoji: String,
    val names: List<String>
) {
    BANGLADESH("BD", "Bangladesh", "🇧🇩", listOf(
        "Arafat Hossain", "Siam Ahmed", "Rakib Hasan", "Tanvir Islam", "Nayeem Hossain",
        "Mahfuzur Rahman", "Sakib Ahmed", "Imran Hossain", "Fahim Hasan", "Rifat Islam",
        "Shakil Ahmed", "Touhidul Islam", "Mehedi Hasan", "Rayhan Hossain", "Jubayer Ahmed",
        "Nazmul Hasan", "Sabbir Rahman", "Adnan Hossain", "Hasan Mahmud", "Nasir Uddin",
        "Farhad Hossain", "Sohag Miah", "Mustafizur Rahman", "Abir Hossain", "Rakibul Islam",
        "Shaon Ahmed", "Tanjim Hasan", "Saiful Islam", "Ashikur Rahman", "Jahid Hasan",
        "Maruf Hossain", "Rubel Ahmed", "Noman Islam", "Ibrahim Hossain", "Tamim Hasan",
        "Rifat Hossain", "Masud Rana", "Kamrul Hasan", "Sadman Islam", "Rezaul Karim",
        "Mijanur Rahman", "Aminul Islam", "Shariful Hasan", "Faysal Ahmed", "Sumon Hossain",
        "Arman Hossain", "Shamim Ahmed", "Sohel Rahman", "Rashedul Islam", "Aonik Hasan",
        "Ishtiaq Hossain", "Rubaiyat Islam", "Mahin Ahmed", "Saklain Hossain", "Nafis Rahman",
        "Tahmid Hasan", "Iftekhar Islam", "Mursalin Hossain", "Samiul Ahmed"
    )),

    UNITED_STATES("US", "United States", "🇺🇸", listOf(
        "James Anderson", "Michael Johnson", "William Smith", "David Williams", "Christopher Brown",
        "Matthew Jones", "Daniel Miller", "Joseph Davis", "Andrew Wilson", "Joshua Moore",
        "Ryan Taylor", "Nicholas Thomas", "Tyler Jackson", "Brandon White", "Justin Harris",
        "Kevin Martin", "Brian Thompson", "Steven Garcia", "Jason Martinez", "Robert Robinson",
        "Ethan Clark", "Noah Lewis", "Alexander Lee", "Benjamin Walker", "Samuel Hall",
        "Daniel Allen", "Jacob Young", "Logan Hernandez", "Mason King", "Lucas Wright",
        "Emily Johnson", "Olivia Smith", "Emma Williams", "Sophia Brown", "Ava Jones",
        "Isabella Miller", "Mia Davis", "Charlotte Wilson", "Amelia Moore", "Harper Taylor",
        "Evelyn Anderson", "Abigail Thomas", "Ella Jackson", "Elizabeth White", "Sofia Harris",
        "Avery Martin", "Scarlett Thompson", "Grace Garcia", "Chloe Martinez", "Victoria Robinson",
        "Lily Clark", "Hannah Lewis", "Natalie Lee", "Zoey Walker", "Samantha Hall",
        "Madison Allen", "Brooklyn Young", "Layla King", "Aria Wright", "Riley Hernandez"
    )),

    CHINA("CN", "China", "🇨🇳", listOf(
        "雅婷 池", "雅君 曾", "雅涵 張", "雅萱 熊", "雅妤 謝", "雅甄 馮", "雅玲 彭", "雅芬 鄧", "雅儀 馬", "雅柔 葉", "雅蓉 武", "雅瑄 向", "雅穎 孫", "雅琪 柯", "雅晴 黎", "雅雯 康", "雅潔 陳", "雅瑜 林", "雅蓁 黃", "雅芸 李", "雅珊 王", "雅慈 吳", "雅茹 劉", "雅嫻 蔡", "雅薇 楊", "雅彤 許", "雅恩 鄭", "雅榕 郭", "雅媛 洪", "雅寧 邱",
        "怡婷 廖", "怡君 賴", "怡涵 徐", "怡萱 周", "怡妤 蘇", "怡甄 莊", "怡玲 呂", "怡芬 江", "怡儀 何", "怡柔 蕭", "怡蓉 羅", "怡瑄 高", "怡穎 潘", "怡琪 簡", "怡晴 朱", "怡雯 鍾", "怡潔 游", "怡瑜 詹", "怡蓁 胡", "怡芸 施", "怡珊 沉", "怡慈 余", "怡茹 盧", "怡嫻 趙", "怡薇 梁", "怡彤 顏", "怡恩 翁", "怡榕 魏", "怡媛 戴", "怡寧 方",
        "欣婷 宋", "欣君 范", "欣涵 杜", "欣萱 傅", "欣妤 侯", "欣甄 曹", "欣玲 薛", "欣芬 丁", "欣儀 卓", "欣柔 董", "欣蓉 唐", "欣瑄 藍", "欣穎 蔣", "欣琪 石", "欣晴 紀", "欣雯 姚", "欣潔 古", "欣瑜 連", "欣蓁 歐", "欣芸 程", "欣珊 湯", "欣慈 田", "欣茹 姜", "欣嫻 汪", "欣薇 白", "欣彤 鄒", "欣恩 尤", "欣榕 巫", "欣媛 鐘", "欣寧 塗",
        "佳婷 龔", "佳君 嚴", "佳涵 韓", "佳萱 袁", "佳妤 阮", "佳甄 金", "佳玲 童", "佳芬 陸", "佳儀 夏", "佳柔 柳", "佳蓉 邵", "佳瑄 錢", "佳穎 温", "佳琪 伍", "佳晴 池", "佳雯 曾", "佳潔 張", "佳瑜 熊", "佳蓁 謝", "佳芸 馮", "佳珊 彭", "佳慈 鄧", "佳茹 馬", "佳嫻 葉", "佳薇 武", "佳彤 向", "佳恩 孫", "佳榕 柯", "佳媛 黎", "佳寧 康",
        "宜婷 陳", "宜君 林", "宜涵 黃", "宜萱 李", "宜妤 王", "宜甄 吳", "宜玲 劉", "宜芬 蔡", "宜儀 楊", "宜柔 許", "宜蓉 鄭", "宜瑄 郭", "宜穎 洪", "宜琪 邱", "宜晴 廖", "宜雯 賴", "宜潔 徐", "宜瑜 周", "宜蓁 蘇", "宜芸 莊", "宜珊 呂", "宜慈 江", "宜茹 何", "宜嫻 蕭", "宜薇 羅", "宜彤 高", "宜恩 潘", "宜榕 簡", "宜媛 朱", "宜寧 鍾",
        "庭婷 游", "庭君 詹", "庭涵 胡", "庭萱 施", "庭妤 沉", "庭甄 余", "庭玲 盧", "庭芬 趙", "庭儀 梁", "庭柔 顏", "庭蓉 翁", "庭瑄 魏", "庭穎 戴", "庭琪 方", "庭晴 宋", "庭雯 范", "庭潔 杜", "庭瑜 傅", "庭蓁 侯", "庭芸 曹", "庭珊 薛", "庭慈 戊", "庭茹 卓", "庭嫻 董", "庭薇 唐", "庭彤 藍", "庭恩 蔣", "庭榕 石", "庭媛 紀", "庭寧 姚",
        "婉婷 古", "婉君 連", "婉涵 歐", "婉萱 程", "婉妤 湯", "婉甄 田", "婉玲 姜", "婉芬 汪", "婉儀 白", "婉柔 鄒", "婉蓉 尤", "婉瑄 巫", "婉穎 鐘", "婉琪 塗", "婉晴 龔", "婉雯 嚴", "婉潔 韓", "婉瑜 袁", "婉蓁 阮", "婉芸 金", "婉珊 童", "婉慈 陸", "婉茹 夏", "婉嫻 柳", "婉薇 邵", "婉彤 錢", "婉恩 温", "婉榕 伍", "婉媛 池", "婉寧 曾",
        "佩婷 張", "佩君 熊", "佩涵 謝", "佩萱 馮", "佩妤 彭", "佩甄 鄧", "佩玲 馬", "佩芬 葉", "佩儀 武", "佩柔 向", "佩蓉 孫", "佩瑄 柯", "佩穎 黎", "佩琪 康", "佩晴 陳", "佩雯 林", "佩潔 黃", "佩瑜 李", "佩蓁 王", "佩芸 吳", "佩珊 劉", "佩慈 蔡", "佩茹 楊", "佩嫻 許", "佩薇 鄭", "佩彤 郭", "佩恩 洪", "佩榕 邱", "佩媛 廖", "佩寧 賴",
        "淑婷 徐", "淑君 周", "淑涵 蘇", "淑萱 莊", "淑妤 呂", "淑甄 江", "淑玲 何", "淑芬 蕭", "淑儀 羅", "淑柔 高", "淑蓉 潘", "淑瑄 簡", "淑穎 朱", "淑琪 鍾", "淑晴 游", "淑雯 詹", "淑潔 胡", "淑瑜 施", "淑蓁 沉", "淑芸 余", "淑珊 盧", "淑慈 趙", "淑茹 梁", "淑嫻 顏", "淑薇 翁", "淑彤 魏", "淑恩 戴", "淑榕 方", "淑媛 宋", "淑寧 范",
        "惠婷 杜", "惠君 傅", "惠涵 侯", "惠萱 曹", "惠妤 薛", "惠甄 己", "惠玲 卓", "惠芬 董", "惠儀 唐", "惠柔 藍", "惠蓉 蔣", "惠瑄 石", "惠穎 紀", "惠琪 姚", "惠晴 古", "惠雯 連", "惠潔 歐", "惠瑜 程", "惠蓁 湯", "惠芸 田", "惠珊 姜", "惠慈 汪", "惠茹 白", "惠嫻 鄒", "惠薇 尤", "惠彤 巫", "惠恩 鐘", "惠榕 塗", "惠媛 龔", "惠寧 严",
        "詩婷 韓", "詩君 袁", "詩涵 阮", "詩萱 金", "詩妤 童", "詩甄 陸", "詩玲 夏", "詩芬 柳", "詩儀 邵", "詩柔 錢", "詩蓉 温", "詩瑄 伍", "詩穎 池", "詩琪 曾", "詩晴 張", "詩雯 熊", "詩潔 謝", "詩瑜 馮", "詩蓁 彭", "詩芸 鄧", "詩珊 馬", "詩慈 葉", "詩茹 武", "詩嫻 向", "詩薇 孫", "詩彤 柯", "詩恩 黎", "詩榕 康", "詩媛 陳", "詩寧 林",
        "筱婷 黃", "筱君 李", "筱涵 王", "筱萱 吳", "筱妤 劉", "筱甄 蔡", "筱玲 楊", "筱芬 許", "筱儀 鄭", "筱柔 郭", "筱蓉 洪", "筱瑄 邱", "筱穎 廖", "筱琪 賴", "筱晴 徐", "筱雯 周", "筱潔 蘇", "筱瑜 莊", "筱蓁 呂", "筱芸 江", "筱珊 何", "筱慈 蕭", "筱茹 羅", "筱嫻 高", "筱薇 潘", "筱彤 簡", "筱恩 朱", "筱榕 鍾", "筱媛 游", "筱寧 詹",
        "郁婷 胡", "郁君 施", "郁涵 沉", "郁萱 余", "郁妤 盧", "郁甄 趙", "郁玲 梁", "郁芬 顏", "郁儀 翁", "郁柔 魏", "郁蓉 戴", "郁瑄 方", "郁穎 宋", "郁琪 范", "郁晴 杜", "郁雯 傅", "郁潔 侯", "郁瑜 曹", "郁蓁 薛", "郁芸 庚", "郁珊 卓", "郁慈 董", "郁茹 唐", "郁嫻 藍", "郁薇 蔣", "郁彤 石", "郁恩 紀", "郁榕 姚", "郁媛 古", "郁寧 連",
        "姿婷 歐", "姿君 程", "姿涵 湯", "姿萱 田", "姿妤 姜", "姿甄 汪", "姿玲 白", "姿芬 鄒", "姿儀 尤", "姿柔 巫", "姿蓉 鐘", "姿瑄 塗", "姿穎 龔", "姿琪 嚴", "姿晴 韓", "姿雯 袁", "姿潔 阮", "姿瑜 金", "姿蓁 童", "姿芸 陸", "姿珊 夏", "姿慈 柳", "姿茹 邵", "姿嫻 錢", "姿薇 温", "姿彤 伍", "姿恩 池", "姿榕 曾", "姿媛 張", "姿寧 熊",
        "鈺婷 謝", "鈺君 馮", "鈺涵 彭", "鈺萱 鄧", "鈺妤 馬", "鈺甄 葉", "鈺玲 武", "鈺芬 向", "鈺儀 孫", "鈺柔 柯", "鈺蓉 黎", "鈺瑄 康", "鈺穎 陳", "鈺琪 林", "鈺晴 黃", "鈺雯 李", "鈺潔 王", "鈺瑜 吳", "鈺蓁 劉", "鈺芸 蔡", "鈺珊 楊", "鈺慈 許", "鈺茹 鄭", "鈺嫻 郭", "鈺薇 洪", "鈺彤 邱", "鈺恩 廖", "鈺榕 賴", "鈺媛 徐", "鈺寧 周",
        "心婷 蘇", "心君 莊"
    )),

    SAUDI_ARABIA("SA", "Saudi Arabia", "🇸🇦", listOf(
        "محمد أحمد", "أحمد محمود", "عبد الله حسن", "عمر محمد", "يوسف علي",
        "خالد حسن", "محمود إبراهيم", "إبراهيم أحمد", "عبد الرحمن محمد", "مصطفى محمود",
        "ياسر أحمد", "سامر حسن", "كريم عبد الله", "طارق محمد", "حمزة علي",
        "أنس محمود", "معاذ أحمد", "زياد حسن", "سليم محمد", "رامي عبد الله",
        "فاطمة أحمد", "مريم محمد", "نور حسن", "سارة محمود", "آمنة علي",
        "زينب أحمد", "ليان محمد", "هبة حسن", "سلمى محمود", "ريم عبد الله",
        "ندى أحمد", "دعاء محمد", "إيمان علي", "آية حسن", "منى محمود",
        "رنا أحمد", "ياسمين محمد", "حنان عبد الله", "مريم علي", "سارة حسن",
        "عبد العزيز أحمد", "عبد الكريم محمد", "عبد الرحيم حسن", "عبد الملك علي", "صالح محمود",
        "فهد أحمد", "ناصر محمد", "سلمان حسن", "وليد عبد الله", "عادل محمود",
        "حسان علي", "رائد أحمد", "باسل محمد", "مازن حسن", "فراس محمود",
        "عمار عبد الله", "سيف أحمد", "إياد محمد", "زاهر حسن", "هاني علي"
    )),

    FRANCE("FR", "France", "🇫🇷", listOf(
        "Jean Martin", "Pierre Bernard", "Louis Dubois", "Thomas Moreau", "Julien Laurent",
        "Nicolas Simon", "Antoine Michel", "Alexandre Lefebvre", "Gabriel Leroy", "Hugo Roux",
        "Lucas Fournier", "Arthur Girard", "Nathan Bonnet", "Maxime Dupont", "Victor Lambert",
        "Adrien Fontaine", "Romain Rousseau", "Paul Vincent", "Julien Chevalier", "Mathieu Robin",
        "Marie Martin", "Camille Bernard", "Emma Dubois", "Chloé Moreau", "Léa Laurent",
        "Manon Simon", "Clara Michel", "Julie Lefebvre", "Alice Leroy", "Louise Roux",
        "Émilie Fournier", "Sophie Girard", "Inès Bonnet", "Sarah Dupont", "Amélie Lambert",
        "Pauline Fontaine", "Charlotte Rousseau", "Juliette Vincent", "Élise Chevalier", "Marion Robin",
        "Benjamin Martin", "Alexandre Bernard", "Thomas Dubois", "Guillaume Moreau", "Sébastien Laurent",
        "Olivier Simon", "François Michel", "Rémi Lefebvre", "Baptiste Leroy", "Clément Roux",
        "Jeanne Fournier", "Margaux Girard", "Mathilde Bonnet", "Valentine Dupont", "Anaïs Lambert",
        "Céline Fontaine", "Noémie Rousseau", "Élodie Vincent", "Amandine Chevalier", "Gabrielle Robin"
    )),

    INDIA("IN", "India (Hindi)", "🇮🇳", listOf(
        "आरव शर्मा", "विवान वर्मा", "आदित्य सिंह", "अर्जुन कुमार", "रोहन गुप्ता",
        "राहुल यादव", "अमित मिश्रा", "अक्षय चौहान", "करण मेहता", "वरुण सक्सेना",
        "मोहित अग्रवाल", "निखिल जैन", "अभिषेक राजपूत", "आकाश तिवारी", "शिवम पांडे",
        "अंकित शुक्ला", "सौरभ त्रिपाठी", "दीपक श्रीवास्तव", "मनीष ठाकुर", "राजीव भट्ट",
        "अनन्या शर्मा", "आर्या वर्मा", "सिया सिंह", "अनुष्का कुमार", "प्रिया गुप्ता",
        "नेहा यादव", "पूजा मिश्रा", "काव्या चौहान", "रिया मेहता", "दिव्या सक्सेना",
        "श्रेया अग्रवाल", "तन्वी जैन", "आयुषी राजपूत", "पायल तिवारी", "राधिका पांडे",
        "निशा शुक्ला", "साक्षी त्रिपाठी", "स्वाति श्रीवास्तव", "मीनाक्षी ठाकुर", "कृति भट्ट",
        "देव शर्मा", "युवराज वर्मा", "आर्यन सिंह", "दक्ष कुमार", "समीर गुप्ता",
        "मयंक यादव", "हर्ष मिश्रा", "विवेक चौहान", "रितेश मेहता", "वरुण अग्रवाल",
        "मुस्कान शर्मा", "नेहा वर्मा", "तनु सिंह", "मानसी कुमार", "आकांक्षा गुप्ता",
        "सोनिया यादव", "ज्योति मिश्रा", "स्नेहा चौहान", "भावना मेहता", "रितिका अग्रवाल"
    )),

    MADAGASCAR("MG", "Madagascar", "🇲🇬", listOf(
        "Jean Rakoto", "Andry Randria", "Hery Rasoanaivo", "Tiana Razafindrakoto", "Fetra Rakotomalala",
        "Tahina Randrianasolo", "Mamy Raveloson", "Tojo Rakotondrabe", "Niry Ramanantsoa", "Lova Razafimahatratra",
        "Solofo Rakotoarisoa", "Njaka Randrianarisoa", "Faly Ramaroson", "Toky Rakotobe", "Zo Andrianjafy",
        "Hasina Razanadrakoto", "Miora Rakotondramasy", "Voahirana Randriamampianina", "Hanitra Rasoazanany", "Fanja Razafindrazaka",
        "Soa Rakotozafy", "Lalao Randrianantenaina", "Anja Ravelomanana", "Malala Rakotondrabe", "Nantenaina Razafindrakoto",
        "Tahina Rakotoarisoa", "Vola Randrianasolo", "Tsiky Rasoanaivo", "Mirana Razafimahatratra", "Sarobidy Rakotomalala",
        "Haja Andrianjafy", "Fenitra Ramaroson", "Kanto Rakotobe", "Iary Ramanantsoa", "Miora Randria",
        "Tovo Rakoto", "Harena Razanadrakoto", "Ony Rakotondramasy", "Mamy Rasoazanany", "Noro Razafindrazaka",
        "Ando Rakotozafy", "Soary Randrianantenaina", "Finaritra Ravelomanana", "Tsiory Rakotondrabe", "Hasina Razafindrakoto",
        "Faneva Rakotoarisoa", "Tantely Randrianasolo", "Lanto Rasoanaivo", "Aina Razafimahatratra", "Miary Rakotomalala",
        "Tsanta Andrianjafy", "Hoby Ramaroson", "Zo Rakotobe", "Fitia Ramanantsoa", "Hanta Randria",
        "Kolo Rakoto", "Ny Aina Razanadrakoto", "Elia Rakotondramasy", "Fara Rasoazanany", "Manda Razafindrazaka"
    ));

    fun getRandomFirstAndLastName(): Pair<String, String> {
        val fullName = names.random()
        val parts = fullName.trim().split(" ")
        val fname = parts.first()
        val lname = if (parts.size > 1) parts.drop(1).joinToString(" ") else "Hossain"
        return Pair(fname, lname)
    }

    companion object {
        fun fromCode(code: String): Country {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: BANGLADESH
        }
    }
}
