
object BoardController {
    fun add(rq: Rq) {
        print("이름 : ")
        var boardName = readLineTrim()
        print("코드 : ")
        var boardId = readLineTrim().toInt()

        var chk = BoardRepository.boardCheck(boardId, boardName)

        if(chk){
            BoardRepository.boardAdd(boardId, boardName)
            println("${boardName} 게시물이 생성 되었습니다.")

        }else{
            println("알림) 이미 만들어진 중복 값이 있습니다.")
        }
    }

    fun list(rq: Rq) {
        println("게시판 코드 / 이름")
        for (i in BoardRepository.boards){
            println("${i.boardId}  /  ${i.boardName}")
        }
    }
}

object SystemController {
    fun exit(rq: Rq){
        println("시스템을 종료합니다.")
    }
}

object MemberController {
    fun join(rq: Rq){
        println("=== 회원 가입 ===")
        print("로그인 아이디 : ")
        var join_id = readLineTrim()
        print("로그인 비밀번호 : ")
        var join_passwd = readLineTrim()
        print("이름 : ")
        var join_name = readLineTrim()
        print("별명 : ")
        var join_nickname= readLineTrim()
        print("휴대전화 : ")
        var join_phone = readLineTrim()
        print("이메일 : ")
        var join_email = readLineTrim()
        var chk:Boolean = MemberRepository.userJoin(join_id, join_passwd, join_name, join_nickname, join_phone, join_email)
        if(chk) println("회원가입이 완료 되었습니다.")
        else println("회원가입 실패")
    }
    fun login(rq: Rq):Member?{
        println("=== 로그인 ===")
        print("로그인 아이디 : ")
        var join_id = readLineTrim()
        print("로그인 비밀번호 : ")
        var join_passwd = readLineTrim()

        var chk = MemberRepository.userLogin(join_id, join_passwd)

        if (chk == null) println("로그인 실패")
        else {
            println("! 로그인 성공! ${chk.login_name} 님 환영합니다.")
            return chk
        }
        return null
    }
    fun logout(rq: Rq):Member?{
        println("로그아웃 되었습니다.")
        return null
    }
}

object ArticleController {
    fun write(rq: Rq,loginMember: Member?){
        if(loginMember == null) println("로그인 이후 사용 가능한 명령어 입니다.")
        else {
            println("===== 게시물 작성 ====")
            print("제목 : ")
            var title = readLineTrim()
            print("내용 : ")
            var contents = readLineTrim()
            print("게시판 :")
            var board_id = readLineTrim().toInt()
            if(BoardRepository.boardCheckInt(board_id)) println("존재하지 않는 게시판 입니다.")
            else {
                ArticleRepository.articleWrite(title, contents, loginMember.login_num, board_id)
                println("글이 정상적으로 등록 되었습니다.")
            }
        }
    }
    fun list(rq: Rq){
        println("번호 /       작성날짜       /       갱신날짜        /   제목   / 작성자 / 게시판")
        for (article in ArticleRepository.articles) {
            var boardName:String =""
            var update = ""
            for(i in BoardRepository.boards){
                if(i.boardId == article.board_id) boardName = i.boardName
            }
            if(article.updateDate == "") update = "        없음        "
            else update = article.updateDate
            println("${article.id} / ${article.regDate} / ${update} / ${article.title} / ${MemberRepository.members[article.member_num].login_id} / ${boardName} ")
        }
    }

    fun detail(rq: Rq){
        val id = rq.getIntParam("id", -1)

        if (id == -1) {
            println("id를 입력해주세요.")
        }else {

            val article = ArticleRepository.getArticleById(id)

            if (article == null) {
                println("${id}번 게시물은 존재하지 않습니다.")
            }else {

                println("번호 : ${article.id}")
                println("작성날짜 : ${article.regDate}")
                println("갱신날짜 : ${article.updateDate}")
                println("제목 : ${article.title}")
                println("내용 : ${article.body}")
            }
        }

    }

    fun delete(rq: Rq,loginMember: Member?){
        if(loginMember == null){
            println("로그인 이후 사용 가능한 명령어 입니다.")
        }else{
            val id = rq.getIntParam("id", -1)
            if (id == -1) {
                println("id를 입력해주세요.")
            }else {
                val article = ArticleRepository.getArticleById(id)
                if (article == null) {
                    println("${id}번 게시물은 존재하지 않습니다.")
                } else if (article.member_num == loginMember!!.login_num) {
                    ArticleRepository.articles.remove(article)
                    println("게시물이 삭제 되었습니다.")
                }
            }
        }
    }

    fun modify(rq: Rq,loginMember: Member?){
        if(MemberRepository.loginCheck(loginMember)) {
            val id = rq.getIntParam("id", -1)

            if (id == -1) {
                println("id를 입력해주세요.")
            }else {

                val article = ArticleRepository.getArticleById(id)

                if (article == null) {
                    println("${id}번 게시물은 존재하지 않습니다.")
                } else if (article.member_num == loginMember!!.login_num) {
                    print("${id}번 게시물 새 제목 : ")
                    val title = readLineTrim()
                    print("${id}번 게시물 새 내용 : ")
                    val body = readLineTrim()

                    ArticleRepository.modifyArticle(id, title, body)

                    println("${id}번 게시물이 수정되었습니다.")
                } else {
                    println("알림) 수정은 본인 글만 가능합니다.")
                }
            }
        }else{
            println("로그인 이후 사용 가능한 명령어입니다.")
        }
    }
}


object sggController{
    fun build(rq: Rq){
        println("모든 목록을 HTML 로 변환합니다.")

        sggRepository.listHtml(rq)
        sggRepository.detailHtml(rq)

    }
}