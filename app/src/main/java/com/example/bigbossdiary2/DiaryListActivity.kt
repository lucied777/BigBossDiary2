package com.example.bigbossdiary2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bigbossdiary2.databinding.ActivityDiaryListBinding
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import java.util.*

class DiaryListActivity : AppCompatActivity(),AlertDeleteDialog.OnAlertDeleteListener  {

    private lateinit var binding : ActivityDiaryListBinding
    private lateinit var realm : Realm

    private var leagueMode = 0
    private var year =  0
    private var teamNumber = 0

    private val arrayTeamName = arrayOf(
        "日本ハムファイターズ","ソフトバンクホークス","西武ライオンズ","楽天イーグルス","千葉ロッテマリーンズ","オリックスバッファローズ",
        "読売ジャイアンツ","広島東洋カープ","阪神タイガース","横浜DeNAベイスターズ","ヤクルトスワローズ","中日ドラゴンズ")
    private val arrayLeague = arrayOf("パリーグ","交流戦","セリーグ","オープン戦")

    /////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
        binding = ActivityDiaryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //インテントのデータ取り出し

        year =  intent.getIntExtra("year",2022)
        teamNumber = intent.getIntExtra("teamNumber",0)
        leagueMode = intent.getIntExtra("leagueMode",0)

        ////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////   各種Viewの設定     /////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////

        binding.apply {

            constLayout.background.alpha = 20

            //チーム名表示

            textTitleTeamName.text = arrayTeamName[teamNumber]

            spinnerLeague.setSelection(leagueMode)

            //リサイクルビューの設定

            val linearLayoutManager = LinearLayoutManager(this@DiaryListActivity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            recycleView.layoutManager = linearLayoutManager

            //スピナー設定

            //※スピナーのリスナーを初回時起動させないための処理
            spinnerLeague.isFocusable = false
            spinnerEnemy.isFocusable = false
            spinnerMonth.isFocusable = false
            spinnerSearch.isFocusable = false

            spinnerLeague.onItemSelectedListener = spinnerListener
            spinnerEnemy.onItemSelectedListener = spinnerListener
            spinnerMonth.onItemSelectedListener = spinnerListener
            spinnerSearch.onItemSelectedListener = spinnerListener

            //各モードのデータ取り出し

            val month = Calendar.getInstance().get(Calendar.MONTH) + 1

            val results: RealmResults<RealmDiaryList>
            val leagueInter = 1
            val leagueModePa = 0
            val leagueModeSe = 2

            when (leagueMode) {
                0, 2 ->
                    results = realm.where<RealmDiaryList>()
                        .equalTo("teamNumber", teamNumber)
                        .between("leagueMode", leagueModePa, leagueModeSe)
                        .equalTo("month",month)
                        .equalTo("year", year).findAll().sort("id", Sort.DESCENDING)
//                2 ->
//                    results = realm.where<RealmDiaryList>()
//                        .equalTo("teamNumber", teamNumber)
//                        .between("leagueMode", leagueInter, leagueMode)
//                        .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                else ->
                    results = realm.where<RealmDiaryList>()
                        .equalTo("teamNumber", teamNumber)
                        .equalTo("leagueMode", leagueMode)
                        .equalTo("month",month)
                        .equalTo("year", year).findAll().sort("id", Sort.DESCENDING)

            }

            textDataSize.text = "検索結果 ${results.size} 件"
            spinnerMonth.setSelection(month)
            spinnerEnemy.setSelection(12)

            when (leagueMode) {
                0,1,2,3 -> recycleView.adapter = DiaryListAdapter(results, true)
                else -> recycleView.adapter = PlayOffAdapter(results, true)
            }

        }
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////
    //spinnerのリスナー ※無名クラス

    val spinnerListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, possition: Int, p3: Long) {

            binding.apply {

                val spinnerLeaguePosition = spinnerLeague.selectedItemPosition
                val spinnerEnemyPosition = spinnerEnemy.selectedItemPosition
                val spinnerMonthPosition = spinnerMonth.selectedItemPosition
                val spinnerSearchPosition = spinnerSearch.selectedItemPosition

                //初期起動時のみリスナーを無効
                if (!spinnerLeague.isFocusable){
                    spinnerLeague.isFocusable = true
                    return
                }
                if (!spinnerEnemy.isFocusable){
                    spinnerEnemy.isFocusable = true
                    return
                }
                if (!spinnerMonth.isFocusable){
                    spinnerMonth.isFocusable = true
                    return
                }
                if (!spinnerSearch.isFocusable){
                    spinnerSearch.isFocusable = true
                    return
                }


                val leagueModePa = 0
                val leagueModeSe = 2
                val leagueInter = 1
                var results: RealmResults<RealmDiaryList>? = null

                when(spinnerLeaguePosition)  {
                    //パリーグ
                    0 -> when(spinnerEnemyPosition){
                        //全チーム
                        12 -> when(spinnerMonthPosition){
                            //全試合
                            0 -> when(spinnerSearchPosition) {
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランが多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                            //各月ごと
                            else -> when(spinnerSearchPosition){
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランが多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                        }
                        //各対戦チームごと
                        else -> when(spinnerMonthPosition){
                            //全試合
                            0 -> when(spinnerSearchPosition) {
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランが多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                            //各月ごと
                            else -> when(spinnerSearchPosition){
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランが多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", spinnerLeaguePosition, leagueInter)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                        }
                    }
                    //セリーグ
                    2 -> when(spinnerEnemyPosition){
                        //全チーム
                        12 -> when(spinnerMonthPosition){
                            //全試合
                            0 -> when(spinnerSearchPosition) {
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランの多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引き分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                            //各月ごと
                            else -> when(spinnerSearchPosition){
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランの多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引き分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                        }
                        //各対戦チームごと
                        else -> when(spinnerMonthPosition){
                            //全試合
                            0 -> when(spinnerSearchPosition) {
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランの多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引き分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                            //各月ごと
                            else -> when(spinnerSearchPosition){
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランの多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引き分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .between("leagueMode", leagueInter, spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                        }
                    }
                    //交流戦オープン戦
                    else -> when(spinnerEnemyPosition){
                        //全チーム
                        12 -> when(spinnerMonthPosition){
                            //全試合
                            0 -> when(spinnerSearchPosition) {
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランの多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引き分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                            //各月ごと
                            else -> when(spinnerSearchPosition){
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランの多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引き分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                        }
                        //各対戦チームごと
                        else -> when(spinnerMonthPosition){
                            //全試合
                            0 -> when(spinnerSearchPosition) {
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランの多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引き分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                            //各月ごと
                            else -> when(spinnerSearchPosition){
                                //日付降順
                                0 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //日付昇順
                                1 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("id")
                                //得点が多い順
                                2 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("getPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //失点が多い順
                                3 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("lostPoint",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //ホームランの多い順
                                4 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("year", year).findAll().sort("homerun",Sort.DESCENDING,"id",Sort.DESCENDING)
                                //勝利した日
                                5 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","◯")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //敗北した日
                                6 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","✕")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //引き分けた日
                                7 -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameResult","引")
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                                //試合評価
                                else -> results = realm.where<RealmDiaryList>()
                                    .equalTo("teamNumber", teamNumber)
                                    .equalTo("leagueMode",spinnerLeaguePosition)
                                    .equalTo("month",spinnerMonthPosition)
                                    .equalTo("vsTeamNumber",spinnerEnemyPosition)
                                    .equalTo("gameValue",spinnerSearchPosition -7)
                                    .equalTo("year", year).findAll().sort("id",Sort.DESCENDING)
                            }
                        }
                    }
                }

                if(results != null){
                    //val adapter = DiaryListAdapter(results, true)
                    when (spinnerLeaguePosition) {
                        0,1,2,3 -> recycleView.adapter = DiaryListAdapter(results, true)
                        else -> recycleView.adapter = PlayOffAdapter(results, true)
                    }
                    textDataSize.text = "検索結果 ${results.size} 件"
                }

            }
        }


        override fun onNothingSelected(p0: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    //リストデータ削除処理　※削除ダイアログからの処理

    override fun onAlertDeleteClick(deleteId: Int) {

        val id = deleteId.toLong()

        realm.executeTransaction{

            val realmDiaryList = realm.where<RealmDiaryList>().equalTo("id",id).findFirst()
            val vsId = realmDiaryList?.vsId
            val shareId = realmDiaryList?.shareId
            val playOffId = realmDiaryList?.playOffId

            realm.where<realmTeamGrade>().equalTo("id",id).findFirst()?.deleteFromRealm()
            realm.where<realmTeamGrade>().equalTo("id",vsId).findFirst()?.deleteFromRealm()

            realm.where<RealmDiaryList>().equalTo("id",id).findFirst()?.deleteFromRealm()
            realm.where<RealmDiaryList>().equalTo("id",vsId).findFirst()?.deleteFromRealm()

            realm.where<RealmDiaryDetail>().equalTo("id",id).findFirst()?.deleteFromRealm()
            realm.where<RealmDiaryDetail>().equalTo("id",vsId).findFirst()?.deleteFromRealm()

            realm.where<RealmShareDiaryDetail>().equalTo("shareId",shareId).findFirst()?.deleteFromRealm()


            Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show()

        }
    }

    /////////////////////////////////////////////////////////////////////////////////

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//        val intent = Intent()
//        setResult(RESULT_OK,intent)
//        finish()
//    }
}