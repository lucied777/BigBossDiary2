package com.example.bigbossdiary2

////////////////////////////////////////////////////////////////////////////////////////////
//listTeamGrade の足し算メソッド

fun plusListTeamGrade(listTeamGrade1 : List<TeamGrade>,listTeamGrade2: List<TeamGrade>) : ArrayList<TeamGrade>{

    val listTeamGrade = ArrayList<TeamGrade>()

    for (i in 0..listTeamGrade1.size-1)
        listTeamGrade.add(listTeamGrade1[i] + listTeamGrade2[i])

    return listTeamGrade
}

/////////////////////////////////////////////////////////////////////////////////////////////
//listTeamGrade の引き算メソッド

fun minusListTeamGrade(listTeamGrade1 : List<TeamGrade>,listTeamGrade2: List<TeamGrade>) : ArrayList<TeamGrade>{

    val listTeamGrade = ArrayList<TeamGrade>()

    for (i in 0..listTeamGrade1.size-1)
        listTeamGrade.add(listTeamGrade1[i] - listTeamGrade2[i])

    return listTeamGrade
}

//////////////////////////////////////////////////////////////////////////////////////////
//realmTeamGradeAppend のプリマリーキー（id）を作成するメソッド

fun makeID( year : Int, leagueMode : Int, teamNumber : Int) : Long{

    val stringBuilder = StringBuilder()
    stringBuilder.append(year).append(leagueMode).append(teamNumber)
    return stringBuilder.toString().toLong()
}

///////////////////////////////////////////////////////////////////////////////////////////
//realmTeamGrade と RealmDiaryList と　RealmDialyDetail のプリマリーキー（id）を作成するメソッド

fun makeID( dateID : String, teamNumber : Int) : Long{

    val stringBuilder = StringBuilder()
    stringBuilder.append(dateID).append(teamNumber)
    return stringBuilder.toString().toLong()
}

///////////////////////////////////////////////////////////////////////////////////////////
//RealmShareDiaryDetail のプリマリーキー（id）を作成するメソッド

fun makeShareID( dateID: String, firstTeamNumber : Int, secondTeamNumber : Int) : Long {

    val stringBuilder = StringBuilder()
    stringBuilder.append(dateID).append(firstTeamNumber).append(secondTeamNumber)
    return stringBuilder.toString().toLong()
}

///////////////////////////////////////////////////////////////////////////////////////////
//RealmPlayOff のプリマリーキー（id）を作成するメソッド

fun makePlayOffID( year : Int, tagPosition : Int) : Long {

    val stringBuilder = StringBuilder()
    return stringBuilder.append(year).append(tagPosition).toString().toLong()

}

fun makePlayOffID( year : Int,leagueMode: Int, tagPosition : Int) : Long {

    val stringBuilder = StringBuilder()
    return stringBuilder.append(year).append(leagueMode).append(tagPosition).toString().toLong()

}

/////////////////////////////////////////////////////////////////////////////////////////
//RealmDiaryList の GameScore( 0 - 3)を作成するメソッド

fun makeGameScore( secondScore : Int, firstScore : Int) : String{

    val stringBuilder = StringBuilder()
    stringBuilder.append(secondScore).append(" ー ").append(firstScore)
    return stringBuilder.toString()
}