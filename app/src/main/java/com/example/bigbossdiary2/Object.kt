package com.example.bigbossdiary2

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.lang.StringBuilder

class TeamGrade (

    var teamNumber : Int, //チームナンバー
    var totalWin : Int, //総勝利数
    var totalLose : Int, //総敗北数
    var totalDraw : Int, //総引き分け数
    var totalGetPoint : Int, //総得点数
    var totalLostPoint : Int, //総失点数
    var totalHomerun : Int, //総ホームラン数

): Comparable<TeamGrade>{
    var rank : Int = 1  //順位
    var totalGame : Int = 0 //総試合数
    var winRate : Double = 0.000 //勝率
    var gameDifference : Double = 0.0 //勝差
    var bank : Int = 0

    override fun compareTo(other: TeamGrade): Int {
        if(this.winRate < other.winRate)
            return  1
        if(this.winRate > other.winRate)
            return  -1
        return  0
    }

    //演算子＋のオーバーロード

    operator fun plus(otherTeamGrade : TeamGrade):TeamGrade{
        return TeamGrade(
            teamNumber ,
            totalWin + otherTeamGrade.totalWin,
            totalLose + otherTeamGrade.totalLose,
            totalDraw + otherTeamGrade.totalDraw,
            totalGetPoint + otherTeamGrade.totalGetPoint,
            totalLostPoint + otherTeamGrade.totalLostPoint,
            totalHomerun + otherTeamGrade.totalHomerun
        )
    }

    //演算子ーのオーバーロード

    operator fun minus(otherTeamGrade : TeamGrade):TeamGrade{
        return TeamGrade(
            teamNumber ,
            totalWin - otherTeamGrade.totalWin,
            totalLose - otherTeamGrade.totalLose,
            totalDraw - otherTeamGrade.totalDraw,
            totalGetPoint - otherTeamGrade.totalGetPoint,
            totalLostPoint - otherTeamGrade.totalLostPoint,
            totalHomerun - otherTeamGrade.totalHomerun
        )
    }

    //演算子＋ーのオーバーロード

    operator fun plusAssign(otherTeamGrade : TeamGrade){

        totalWin +- otherTeamGrade.totalWin
        totalLose +- otherTeamGrade.totalLose
        totalDraw +- otherTeamGrade.totalDraw
        totalGetPoint +- otherTeamGrade.totalGetPoint
        totalLostPoint +- otherTeamGrade.totalLostPoint
        totalHomerun +- totalHomerun

    }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
//

open class realmTeamGrade : RealmObject(){
    @PrimaryKey
    var id : Long = 202200000
    var teamNumber : Int = 0
    var gameResult : String = "◯"
    var getPoint : Int = 0
    var lostPoint : Int = 0
    var homerun : Int = 0
    var playOffStage : Int = 0 //削除用　0,1,2 FirstStage , 3,4,5,6,7,8 FinalStage, 9,10,11,12,13,14,15 日本シリーズ
    var leagueMode : Int = 0
    var year : Int = 2022
}

///////////////////////////////////////////////////////////////////////////////////////////////////
//

open class realmTeamGradeAppend : RealmObject(){
    @PrimaryKey
    var id : Long = 202200000
    var teamNumber : Int = 0 //チームナンバー
    var totalWin : Int = 0//総勝利数
    var totalLose : Int = 0//総敗北数
    var totalDraw : Int = 0 //総引き分け数
    var totalGetPoint : Int = 0//総得点数
    var totalLostPoint : Int = 0//総失点数
    var totalHomerun : Int = 0//総ホームラン数
    var leagueMode : Int = 0
    var year : Int = 2022
}

///////////////////////////////////////////////////////////////////////////////////////////////////
//

open class RealmDiaryList : RealmObject(){
    @PrimaryKey
    var id : Long = 202200000
    var date : String = "2022年02月3日"
    var teamNumber : Int = 0
    var vsTeamNumber : Int = 1 //相手チーム ,検索用
    var title : String = "ノータイトル"
    var gameResult : String = "◯"
    var secondTeamNumber : Int = 0
    var gameScore : String = "3 - 2"
    var firstTeamNumber : Int = 1
    var homerun : Int = 0
    var bestPlayer : String = ""
    var gameValue : Int = 0 //試合の評価　Best、Worstなど
    var vsId: Long = 202200000 //相手チームのID、削除用
    var playOffId : Long = 202240 //削除用
    var playOffStage : Int = 99 //削除用　0,1,2 FirstStage , 3,4,5,6,7,8 FinalStage, 9,10,11,12,13,14,15 日本シリーズ　
    var getPoint : Int = 0 //検索用
    var lostPoint : Int = 0 //検索用
    var shareId : Long = 2022000000 //共通データのId　※RealmShareDiaryDetailのId,　削除用
    var month : Int = 2 //検索用
    var year : Int = 2022
    var leagueMode = 0
}

///////////////////////////////////////////////////////////////////////////////////////////////////
//

open class RealmDiaryDetail : RealmObject(){
    @PrimaryKey
    var id : Long = 202200000
    var title : String = "" //日記タイトル
    var diaryBody : String = "" //日記本文
    var bestPlayer : String = ""
    var gameValue : Int = 0 //試合の評価　Best、Worstなど
    var image : ByteArray? = null //画像
    var playOffStage : Int = 0 //削除用　0,1,2 FirstStage , 3,4,5,6,7,8 FinalStage, 9,10,11,12,13,14,15 日本シリーズ
    var leagueMode = 0 //プレイオフデータの削除用
    var year : Int = 2022
}

///////////////////////////////////////////////////////////////////////////////////////////////////
//

open class RealmShareDiaryDetail : RealmObject(){
    @PrimaryKey
    var shareId : Long = 2022000000
    var date : String = "2022年3月19日"
    var place : String = ""
    var scoreBoard : String = ",,,,0,0,0,0,0,0,0,0,0,,,,,0,0,0,0,0,0,0,0,0,,,,"
    var winPitcher : String = "" //勝利投手
    var losePitcher : String = "" //敗戦投手
    var firstTeamId : Long = 0
    var secondTeamId : Long = 0
    var firstHomerunBatter : String = "" //先行チームホームラン
    var secondHomerunBatter : String = "" //後攻チームホームラン
    var playOffStage : Int = 0 //削除用　0,1,2 FirstStage , 3,4,5,6,7,8 FinalStage, 9,10,11,12,13,14,15 日本シリーズ
    var leagueMode = 0 //プレイオフデータの削除用
    var year : Int = 2022
}

///////////////////////////////////////////////////////////////////////////////////////////////////
//

open class RealmPlayOff : RealmObject(){
    @PrimaryKey
    var playOffId : Long = 202240
    var firstTeamNumber : Int = 0 //先行チーム
    var secondTeamNumber : Int = 1 //後攻チーム

    var year : Int = 1022
}