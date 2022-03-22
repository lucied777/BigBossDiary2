package com.example.bigbossdiary2

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bigbossdiary2.databinding.FragmentPaleagueBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import java.util.*
import kotlin.collections.ArrayList

class PaleagueFragment : Fragment(), AlertSaveDialog.OnAlertSaveListener{

    private lateinit var  realm : Realm
    private var _binding : FragmentPaleagueBinding? = null
    private val binding: FragmentPaleagueBinding get() = _binding!!

    private var listTeamGrade = ArrayList<TeamGrade>() //各モードのデータに交流戦のエディット追加データだけを含めたデータ　※エディットモードの最小値になる
    private var listTeamGradeAppend = ArrayList<TeamGrade>() //listTeamGradeにさらに各モードのエディット追加データを含めたデータ
    private var listTeamGradeEdit = ArrayList<TeamGrade>() //エディット時に使う変更してもいいデータ

    private lateinit var adapterTeamGrade: TeamGradeAdapter

    private var year : Int = 2020
    private var leagueMode = 0 //パリーグ

    private var position = 0 //リスト選択のチームナンバーが入る変数
    private var switchScore = true //得点　true　失点　false
    private var switchWinRate = true //ゲーム差　true　勝率　false

    val arrayTeamName = arrayOf(
        "日本ハムファイターズ","ソフトバンクホークス","西武ライオンズ","楽天イーグルス","千葉ロッテマリーンズ","オリックスバッファローズ")

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()

        arguments?.let {
            year = it.getInt("year")
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPaleagueBinding.inflate(inflater,container,false)

        ////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////   各種Viewの設定     /////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////

        binding.apply {

            //layoutMain2.background.alpha = 20
            textLeague.text = "パリーグ順位"
            textYear.text = "$year"

            //リサイクルビュー設定

            recycleView.apply{
                layoutManager = LinearLayoutManager(context)
                adapterTeamGrade = TeamGradeAdapter(leagueMode,year).apply {
                    setOnClickRecycleListener (
                        {onLongClickRecycleListener(it) }
                    )}
                adapter = adapterTeamGrade
            }

            //リセットボタン
            buttonReset.setOnClickListener { makeListTeamGrade() }

            //確認ボタン
            buttonCheck.setOnClickListener { onButtonCheck()}

            //保存ボタン
            buttonEditSave.setOnClickListener {
                val alertSaveDialog = AlertSaveDialog()
                alertSaveDialog.show(childFragmentManager,"保存ボタン")
            }

            //得点・失点切り替えボタン
            buttonScore.setOnClickListener {
                switchScore = !switchScore

                if(switchScore){
                    buttonScore.text = "失点表示"
                    textScore.text ="得点"}
                else {
                    buttonScore.text = "得点表示"
                    textScore.text = "失点"
                }
                adapterTeamGrade.setList(switchScore,switchWinRate)
                binding.recycleView.adapter = adapterTeamGrade
            }

            //勝率・ゲーム差切り替えボタン
            buttonWinRate.setOnClickListener {

                switchWinRate = !switchWinRate
                if(switchWinRate){
                    buttonWinRate.text = "勝率表示"
                    textWinRate.text = "ゲーム差"}
                else {
                    buttonWinRate.text = "ゲーム差\n表示"
                    textWinRate.text = "勝率"}

                adapterTeamGrade.setList(switchScore,switchWinRate)
                binding.recycleView.adapter = adapterTeamGrade
            }
        }
        return binding.root
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //チーム成績表示
        makeListTeamGrade()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////  各ArrayList<TeamGrade>インスタンスを作成  ///////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //　データベース realmTeamGrade　から 各モードと交流戦のデータを作成

    fun makeListTeamGrade() {

        var listData = ArrayList<TeamGrade>()

        val leagueInter : Int = 1 //交流戦

        for (teamNumber in 0..5){
            var realmTeamGrade = realm.where<realmTeamGrade>()
                .equalTo("teamNumber",teamNumber).equalTo("year",year)
                .between("leagueMode", leagueMode,leagueInter).findFirst()

            if (realmTeamGrade != null){ //チームデータが一個でもあるならインスタンスを作る

                var totalWin = realm.where<realmTeamGrade>()
                    .equalTo("teamNumber",teamNumber).equalTo("gameResult","◯").equalTo("year",year)
                    .between("leagueMode", leagueMode,leagueInter).findAll().size

                var totalLose = realm.where<realmTeamGrade>()
                    .equalTo("teamNumber",teamNumber).equalTo("gameResult","✕").equalTo("year",year)
                    .between("leagueMode", leagueMode,leagueInter).findAll().size

                var totalDraw = realm.where<realmTeamGrade>()
                    .equalTo("teamNumber",teamNumber).equalTo("gameResult","引").equalTo("year",year)
                    .between("leagueMode", leagueMode,leagueInter).findAll().size

                var totalGetPoint = realm.where<realmTeamGrade>()
                    .equalTo("teamNumber",teamNumber).between("leagueMode", leagueMode,leagueInter)
                    .equalTo("year",year).findAll().sum("getPoint")

                var totalLostPoint = realm.where<realmTeamGrade>()
                    .equalTo("teamNumber",teamNumber).between("leagueMode", leagueMode,leagueInter)
                    .equalTo("year",year).findAll().sum("lostPoint")

                var totalHomerun = realm.where<realmTeamGrade>()
                    .equalTo("teamNumber",teamNumber).between("leagueMode", leagueMode,leagueInter)
                    .equalTo("year",year).findAll().sum("homerun")

                listData.add(TeamGrade(teamNumber,totalWin,totalLose,totalDraw,totalGetPoint.toInt(),totalLostPoint.toInt(),totalHomerun.toInt()))

            }else{ //データが無いなら全て０のデータを作成
                listData.add(TeamGrade(teamNumber,0,0,0,0,0,0 ))
            }
        }
        makeListTeamGradeAppend(listData)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //　データベース realmTeamGradeAppend　から listData に追加データを含めたデータを作成

    fun makeListTeamGradeAppend(listData : List<TeamGrade>) {

        //各arryayList<TeamGrade>の初期化

        listTeamGrade = ArrayList<TeamGrade>()
        listTeamGradeEdit = ArrayList<TeamGrade>()
        listTeamGradeAppend = ArrayList<TeamGrade>()

        //　listTeamGrade　を作成
        // ※交流戦のエディットモードで作成した追加データを含めたデータ

        val leagueInter = 1 //交流戦

        //交流戦の追加データを取得

        var realmResult = realm.where<realmTeamGradeAppend>().equalTo("leagueMode", leagueInter).equalTo("year", year).findAll().sort("teamNumber")

        var listDataAppend = ArrayList<TeamGrade>()  //仮の交流戦データ追加リスト変数

        for (realmTeamGradeAppend in realmResult) {
            realmTeamGradeAppend.apply {
                listDataAppend.add(TeamGrade(teamNumber,totalWin,totalLose, totalDraw,totalGetPoint,totalLostPoint, totalHomerun))
            }
        }

        listTeamGrade = plusListTeamGrade(listData , listDataAppend)

        // listTeamGradeAppend、listTeamGradeEditを作成　
        // ※listTeamGradeにさらに各モードのエディットモード作成したデータを含めたデータ

        realmResult = realm.where<realmTeamGradeAppend>()
            .equalTo("leagueMode",leagueMode).equalTo("year",year).findAll().sort("teamNumber")

        listDataAppend = ArrayList<TeamGrade>()

        for (realmTeamGradeAppend in realmResult){
            realmTeamGradeAppend.apply {
                listDataAppend.add(TeamGrade(teamNumber,totalWin,totalLose,totalDraw,totalGetPoint,totalLostPoint,totalHomerun ))
            }
        }
        listTeamGradeAppend = plusListTeamGrade(listTeamGrade , listDataAppend)
        listTeamGradeEdit = plusListTeamGrade(listTeamGrade , listDataAppend)

        makeListTeamGradeFinal(listTeamGradeAppend)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //　listTeamGradeAppend　から順位、総試合数、ゲーム差などを含め勝率順に並び替えた最終的なデータを作成

    fun makeListTeamGradeFinal(listTeamGrade: ArrayList<TeamGrade>) {
        var max = 0
        for (teamGrade in listTeamGrade){
            teamGrade.apply {

                //総試合数
                totalGame = totalWin + totalLose + totalDraw

                //勝率
                if(totalWin + totalLose != 0)
                    winRate = (totalWin.toDouble()/(totalWin + totalLose))
                else winRate = 0.0

                //貯金とMAX値
                bank = totalWin -totalLose
                if (max < bank) max = bank
            }
        }

        //ゲーム差
        for (teamGrade in listTeamGrade){
            teamGrade.gameDifference = (max - teamGrade.bank)/2.0
        }

        //順位
        var i = 0
        while (i < listTeamGrade.size){
            listTeamGrade[i].rank = 1
            var j = 0
            while (j < listTeamGrade.size){
                if (listTeamGrade[i].winRate < listTeamGrade[j].winRate)
                    listTeamGrade[i].rank = listTeamGrade[i].rank + 1
                j++
            }
            i++
        }
        //勝率順に並び替え
        Collections.sort(listTeamGrade)

        show(listTeamGrade)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //リサイクルビューの表示メソッド

    fun show(listTeamGrade : List<TeamGrade>){

        adapterTeamGrade.setList(listTeamGrade,switchScore,switchWinRate)
        binding.recycleView.adapter = adapterTeamGrade
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //リサイクルビューを短く押ししたときの処理

//    fun onShortClickRecycleListener(teamNumber : Int){
//
//        onShortClickToDiaryListActivity(teamNumber)
//    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //リサイクルビューを長押ししたときの処理

    fun onLongClickRecycleListener(teamGrade: TeamGrade){

        binding.apply {

            //選択されたチームナンバーを取得
            position = teamGrade.teamNumber

            textTeamName.text = arrayTeamName[position]

            //総勝利数
            numPickerWin.apply {
                maxValue = 150
                minValue = listTeamGrade[position].totalWin
                value = teamGrade.totalWin
            }

            //総敗北数
            numPickerLose.apply {
                maxValue = 150
                minValue = listTeamGrade[position].totalLose
                value = teamGrade.totalLose
            }

            //総引き分け数
            numPickerDraw.apply {
                maxValue = 150
                minValue = listTeamGrade[position].totalDraw
                value = teamGrade.totalDraw
            }

            //総得点数
            numPickerGetPoint.apply {
                maxValue = 900
                minValue = listTeamGrade[position].totalGetPoint
                value = teamGrade.totalGetPoint
            }

            //総失点数
            numPickerLostPoint.apply {
                maxValue = 900
                minValue = listTeamGrade[position].totalLostPoint
                value = teamGrade.totalLostPoint
            }

            //総ホームラン数
            numPickerHomerun.apply {
                maxValue = 900
                minValue = listTeamGrade[position].totalHomerun
                value = teamGrade.totalHomerun
            }

            buttonCheck.isEnabled = true
            buttonCheck.setBackgroundResource(R.drawable.bottontype)

            buttonReset.isEnabled = true
            buttonReset.setBackgroundResource(R.drawable.bottontype)

            buttonEditSave.isEnabled = true
            buttonEditSave.setBackgroundResource(R.drawable.bottontype)

        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //エディット確認ボタンの処理

    fun onButtonCheck(){

        //エディットデータをチームナンバー順に並べ直す
        binding.apply {
            listTeamGradeEdit.sortWith{ a, b ->
                if (a.teamNumber < b.teamNumber)
                    -1
                else if (a.teamNumber > b.teamNumber)
                    1
                else 0
            }
            listTeamGradeEdit[position].totalWin = numPickerWin.value
            listTeamGradeEdit[position].totalLose = numPickerLose.value
            listTeamGradeEdit[position].totalDraw = numPickerDraw.value
            listTeamGradeEdit[position].totalGetPoint = numPickerGetPoint.value
            listTeamGradeEdit[position].totalLostPoint = numPickerLostPoint.value
            listTeamGradeEdit[position].totalHomerun = numPickerHomerun.value
        }
        makeListTeamGradeFinal(listTeamGradeEdit)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //エディット保存ボタンの処理 ※アラート保存ダイアログからの処理

    override fun onAlertSaveClick() {

        //エディットデータをチームナンバー順に並べ直す

        listTeamGradeEdit.sortWith{ a, b ->
            if (a.teamNumber < b.teamNumber)
                -1
            else if (a.teamNumber > b.teamNumber)
                1
            else 0
        }

        //エディットデータから追加データを含んでいないデータを引いて追加データを求める
        val listDataEdit = minusListTeamGrade(listTeamGradeEdit,listTeamGrade)

        binding.apply {

            realm.executeTransaction{
                val realmResult = realm.where<realmTeamGradeAppend>().equalTo("leagueMode",leagueMode)
                    .equalTo("year",year).findAll().sort("teamNumber")

                var teamNumber = 0
                for (realmTeamGradeAppend in realmResult ){
                    realmTeamGradeAppend.apply {
                        totalWin = listDataEdit[teamNumber].totalWin
                        totalLose = listDataEdit[teamNumber].totalLose
                        totalDraw = listDataEdit[teamNumber].totalDraw
                        totalGetPoint = listDataEdit[teamNumber].totalGetPoint
                        totalLostPoint = listDataEdit[teamNumber].totalLostPoint
                        totalHomerun = listDataEdit[teamNumber].totalHomerun
                    }
                    teamNumber++
                }
            }
            //(activity as TeamGradeActivity).makeViewPager2Adapter()
            makeListTeamGrade()
        }
        Snackbar.make(binding.root,"保存しました",Snackbar.LENGTH_LONG).show()
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    companion object {
        @JvmStatic
        fun newInstance(year: Int) =
            PaleagueFragment().apply {
                arguments = Bundle().apply {
                    putInt("year", year)
                }
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}