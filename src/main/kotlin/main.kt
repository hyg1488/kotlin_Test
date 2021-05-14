import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat

fun main() {
    var loginMember:Member? = null
    println(" ======== 프로그램 시작 =========")
    try {
        DataRepository.dataLodding()
    }catch(e:Exception){}
    while (true) {
        if(loginMember != null) println("현재 ${loginMember.login_id}님 로그인 상태")
        print("명령어 입력 : ")
        var command = readLineTrim()
        var rq = Rq(command)
        when (rq.actionPath) {
            "/system/exit" -> {
                SystemController.exit(rq)
                break
            }
            "/article/list" -> {
                ArticleController.list(rq)
            }
            "/article/detail" -> {
                ArticleController.detail(rq)
            }
            "/article/modify" -> {
                ArticleController.modify(rq,loginMember)
                DataRepository.dataStore()
            }
            "/article/delete" -> {
                ArticleController.delete(rq,loginMember)
            }
            "/article/write" -> {
                ArticleController.write(rq,loginMember)
                DataRepository.dataStore()
            }

            "/member/join" -> {
                MemberController.join(rq)
                DataRepository.dataStore()
            }

            "/member/login" -> {
                loginMember = MemberController.login(rq)
            }
            "/member/logout" -> {
                loginMember = MemberController.logout(rq)
            }
            "/board/add" -> {
                BoardController.add(rq)
                DataRepository.dataStore()
            }
            "/board/list" -> {
                BoardController.list(rq)
            }
            "/sgg/build"->{
                sggController.build(rq)
            }
            else -> {
                println("${command}는 존재하지 않는 명령어입니다.")
            }

        }

    }
}





// Rq는 UserRequest의 줄임말이다.
// Request 라고 하지 않은 이유는, 이미 선점되어 있는 클래스명 이기 때문이다.
class Rq(command: String) {
    // 데이터 예시
    // 전체 URL : /artile/detail?id=1
    // actionPath : /artile/detail
    val actionPath: String

    // 데이터 예시
    // 전체 URL : /artile/detail?id=1&title=안녕
    // paramMap : {id:"1", title:"안녕"}
    private val paramMap: Map<String, String>

    // 객체 생성시 들어온 command 를 ?를 기준으로 나눈 후 추가 연산을 통해 actionPath와 paramMap의 초기화한다.
    // init은 객체 생성시 자동으로 딱 1번 실행된다.
    init {
        // ?를 기준으로 둘로 나눈다.
        val commandBits = command.split("?", limit = 2)

        // 앞부분은 actionPath
        actionPath = commandBits[0].trim()

        // 뒷부분이 있다면
        val queryStr = if (commandBits.lastIndex == 1 && commandBits[1].isNotEmpty()) {
            commandBits[1].trim()
        } else {
            ""
        }

        paramMap = if (queryStr.isEmpty()) {
            mapOf()
        } else {
            val paramMapTemp = mutableMapOf<String, String>()

            val queryStrBits = queryStr.split("&")

            for (queryStrBit in queryStrBits) {
                val queryStrBitBits = queryStrBit.split("=", limit = 2)
                val paramName = queryStrBitBits[0]
                val paramValue = if (queryStrBitBits.lastIndex == 1 && queryStrBitBits[1].isNotEmpty()) {
                    queryStrBitBits[1].trim()
                } else {
                    ""
                }

                if (paramValue.isNotEmpty()) {
                    paramMapTemp[paramName] = paramValue
                }
            }

            paramMapTemp.toMap()
        }
    }

    fun getStringParam(name: String, default: String): String {
        return paramMap[name] ?: default
    }

    fun getIntParam(name: String, default: Int): Int {
        return if (paramMap[name] != null) {
            try {
                paramMap[name]!!.toInt()
            } catch (e: NumberFormatException) {
                default
            }
        } else {
            default
        }
    }
}



data class Board(
    var boardId:Int,
    var boardName:String
){}

data class Article(
    val id: Int,
    val regDate: String,
    var updateDate: String,
    var title: String,
    var body: String,
    var member_num:Int,
    var board_id:Int
){}

data class Member(
    var login_num:Int,
    var login_id:String ,
    var login_passwd:String,
    var login_name:String,
    var login_nickname:String,
    var login_tell:String,
    var login_email:String
){}

object DataRepository{
    var mapper = jacksonObjectMapper()
    var articleRoute = "src/main/Data/article.json"
    var memberRoute = "src/main/Data/member.json"
    var boardRoute = "src/main/Data/board.json"
    fun dataStore(){
        mapper.writerWithDefaultPrettyPrinter().writeValue(
            File(articleRoute),
            ArticleRepository.articles)
        mapper.writerWithDefaultPrettyPrinter().writeValue(
            File(memberRoute),
            MemberRepository.members)
        mapper.writerWithDefaultPrettyPrinter().writeValue(
            File(boardRoute),
            BoardRepository.boards)
    }

    fun dataLodding(){
        MemberRepository.members = mapper.readValue<ArrayList<Member>>(File(memberRoute))
        ArticleRepository.articles = mapper.readValue<ArrayList<Article>>(File(articleRoute))
        BoardRepository.boards = mapper.readValue<ArrayList<Board>>(File(boardRoute))
        ArticleRepository.id = ArticleRepository.articles.lastIndex+1
        MemberRepository.num = MemberRepository.members.lastIndex+1
    }
}
object MemberRepository {
    var num = 0
    var members = mutableListOf<Member>()
    fun userJoin(join_id:String, join_passwd:String, join_name:String, join_nickname:String, join_phone:String, join_email:String):Boolean{
        for(member in members){
            if(member.login_id == join_id){
                println("${join_id} 해당 아이디는 중복되는 아이디가 있어서 사용이 불가능 합니다.")
                return false
            }else if(member.login_nickname == join_nickname){
                println("${join_nickname} 해당 닉네임은 중복되는 닉네임이 있어서 사용이 불가능 합니다.")
                return false
            }
        }
        println("정상적으로 가입 되었습니다.")
        members.add(Member(num,join_id,join_passwd,join_name,join_nickname,join_phone,join_email))
        num++
        return true
    }
    fun userLogin(join_id:String, join_passwd:String):Member?{
        for(member in members){
            if(member.login_id == join_id){
                if(member.login_passwd == join_passwd){
                    return member
                }else{
                    println("비밀번호를 다시 확인해주세요.")
                }
            }else{
                println("아이디 값을 다시 확인해주세요.")
            }
        }
        return null
    }
    fun makeTestMember(){
        for(i in 0 .. 10){
            var id = "admin$i"
            var pass = "admin"
            var name = "admin$i"
            var tell = "비공개"
            var email = "admin@admin.com"
            userJoin(id,pass, name, "관리자$i",tell,email)
        }
    }
    fun loginCheck(login_info:Member?):Boolean{
        var chk:Boolean = false
        if(login_info ==null){
            println("알림) 해당 명령어는 로그인 후 이용해주세요.")
        }else {
            chk = true
        }
        return chk
    }
}


object ArticleRepository {
    var id = 0
    var articles = mutableListOf<Article>()

    fun articleWrite(title: String, contents:String,member_num: Int,board_id: Int){
        var nowDate = Util.getNowDateStr()
        articles.add(Article(id,nowDate,"",title,contents,member_num,board_id))
        id++
    }
    fun modifyArticle(id: Int, title: String, body: String) {
        val article = getArticleById(id)!!
        article.title = title
        article.body = body
        article.updateDate = Util.getNowDateStr()
    }
    fun makeTestArticles() {
        for (id in 1..100) {
            val title = "제목$id"
            val body = "내용_$id"
            articleWrite(title, body,0,0)
        }
    }
    fun getArticleById(id: Int): Article? {
        for (article in articles) {
            if (article.id == id) {
                return article
            }
        }
        return null
    }
}


object BoardRepository {
    var boards = mutableListOf<Board>()
    fun boardCheckInt(board_id: Int):Boolean{
        for(i in boards){
            if(i.boardId == board_id) return false
        }
        return true
    }
    fun boardCheck(board_id: Int, boardName:String):Boolean{
        for(i in boards){
            if(i.boardId == board_id) return false
            else if(i.boardName == boardName) return false

        }
        return true
    }

    fun boardAdd(board_id: Int, boardName:String){
        boards.add(Board(board_id, boardName))
    }

}

object sggRepository{

    fun listHtml(rq: Rq){
        var articleListhHtml = "<!doctype html>\n" +
                "<html lang=\"ko\">\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>CSS</title>\n" +
                "    <style>\n" +
                "        table {\n" +
                "            width: 100%;\n" +
                "            border: 1px solid #444444;\n" +
                "        }\n" +
                "\n" +
                "        th {\n" +
                "            border: 1px solid #444444;\n" +
                "        }\n" +
                "        .name{\n" +
                "            width: 300px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body><table>\n" +
                "        <thead>\n" +
                "            <tr>\n" +
                "                <th>번호 </th>\n" +
                "                <th>작성날짜</th>\n" +
                "                <th>갱신날짜</th>\n" +
                "                <th class =\"name\">제목</th>\n" +
                "                <th>작성자</th>\n" +
                "                <th>게시판</th>\n" +
                "            </tr>\n" +
                "        </thead>" +
                "        <tbody>"
        for (article in ArticleRepository.articles) {
            var boardName:String =""
            var update = ""
            var detailCnt =0
            for(i in BoardRepository.boards){
                if(i.boardId == article.board_id) boardName = i.boardName
            }
            update = if(article.updateDate == "") "        없음        " else article.updateDate
            articleListhHtml += "<tr>\n" +
                    "                <td>${article.id+1}</td>\n" +
                    "                <td>${article.regDate}</td>\n" +
                    "                <td>${update}</td>\n" +
                    "                <td><a href=\"detail${detailCnt}.html\"> ${article.title}</a></td>\n" +
                    "                <td>${MemberRepository.members[article.member_num].login_id}</td>\n" +
                    "                <td> ${boardName} </td>\n" +
                    "            </tr>"
            detailCnt++
        }
        articleListhHtml+="        </tbody>\n" +
                "    </table>\n" +
                "</body>\n" +
                "\n" +
                "</html>"
        //파일 저장
        val path = "src/main/html/article.html";
        try{
            Files.write(Paths.get(path), articleListhHtml.toByteArray(), StandardOpenOption.CREATE)
            print("ArticleList 변환 성공")
        }catch (e:Exception) {
            print("에러!")
        }
    }

    fun detailHtml(rq: Rq){
        var detailCnt = 0;
        for (article in ArticleRepository.articles) {
            var boardName: String = ""
            var update = ""
            for (i in BoardRepository.boards) {
                if (i.boardId == article.board_id) boardName = i.boardName
            }
            update = if (article.updateDate == "") "        없음        " else article.updateDate
            val articleDetailHtml = "<!doctype html>\n" +
                    "<html lang=\"ko\">\n" +
                    "\n" +
                    "<head>\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <title>CSS</title>\n" +
                    "    <style>\n" +
                    "        table {\n" +
                    "            width: 100%;\n" +
                    "            border: 1px solid #444444;\n" +
                    "        }\n" +
                    "        td{\n" +
                    "            border: 1px solid #444444;\n" +
                    "        }\n" +
                    "        .contents{\n" +
                    "            height: 400px;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "\n" +
                    "<body>\n" +
                    "    <table>\n" +
                    "        <tbody>\n" +
                    "            <tr>\n" +
                    "                <td colspan=\"1\">번호 </td>\n" +
                    "                <td colspan=\"1\">${article.id+1}</td>\n" +
                    "                <td >제목</td>\n" +
                    "                <td colspan=\"2\">${article.title}</td>\n" +
                    "            </tr>\n" +
                    "            <tr>\n" +
                    "                <td colspan=\"1\">작성 날짜 </td>\n" +
                    "                <td >${article.regDate}</td>\n" +
                    "                <td >갱신 날짜</td>\n" +
                    "                <td colspan=\"4\">${update}</td>\n" +
                    "            </tr>\n" +
                    "<tr>\n" +
                    "                <td colspan=\"1\">작성자 </td>\n" +
                    "                <td >${MemberRepository.members[article.member_num].login_id}</td>\n" +
                    "                <td >게시판</td>\n" +
                    "                <td colspan=\"4\">  ${boardName} </td>\n" +
                    "            </tr>"+
                    "            <tr>\n" +
                    "                <th colspan=\"4\">내용 </th>\n" +
                    "             \n" +
                    "            </tr> \n" +
                    "            <tr>\n" +
                    "                <td colspan=\"4\" class=\"contents\">${article.body}</td>\n" +
                    "            </tr>\n" +
                    "        </tbody>\n" +
                    "    </table>\n" +
                    "    <a href=\"article.html\">돌아가기</a>\n" +
                    "\n" +
                    "</body>\n" +
                    "\n" +
                    "</html>"
            val path = "src/main/html/detail${detailCnt}.html";
            try{
                Files.write(Paths.get(path), articleDetailHtml.toByteArray(), StandardOpenOption.CREATE)
                println("${detailCnt}번 게시판 변환 성공")
                detailCnt++
            }catch (e:Exception) {
                print("에러!")
            }
        }
    }

}


// 유틸 관련 시작
fun readLineTrim() = readLine()!!.trim()

object Util {
    fun getNowDateStr(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format.format(System.currentTimeMillis())
    }
}
// 유틸 관련 끝