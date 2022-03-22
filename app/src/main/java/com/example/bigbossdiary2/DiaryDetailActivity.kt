package com.example.bigbossdiary2

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.bigbossdiary2.databinding.ActivityDiaryDetailBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_diary_detail.*
import kotlinx.android.synthetic.main.activity_diary_detail.view.*
import kotlinx.android.synthetic.main.card_layout3.*

class DiaryDetailActivity : AppCompatActivity(), AlertSaveDialog.OnAlertSaveListener{

    private lateinit var binding : ActivityDiaryDetailBinding

    private lateinit var realm : Realm

    private var id : Long = 0
    private var vsId : Long = 0
    private var shareId : Long = 0

    private lateinit var arrayEditText : Array<EditText>

    private var realmDiaryList : RealmDiaryList? = null
    private var realmDiaryDetail : RealmDiaryDetail? = null
    private var realmShareDiaryDetail : RealmShareDiaryDetail? = null

    private var realmTeamGradeFirst : realmTeamGrade? = null
    private var realmTeamGradeSecond : realmTeamGrade? = null

    private var realmDiaryListFirst : RealmDiaryList? = null
    private var realmDiaryListSecond : RealmDiaryList? = null

    private val arrayTeamName = arrayOf("日本ハム","ソフバン","西武","楽天","ロッテ","オリックス","巨人","広島","阪神","横浜","ヤクルト","中日")
    private val arrayTitleTeamName = arrayOf(
        "日本ハムファイターズ","ソフトバンクホークス","西武ライオンズ","楽天イーグルス","千葉ロッテマリーンズ","オリックスバッファローズ",
        "読売ジャイアンツ","広島東洋カープ","阪神タイガース","横浜DeNAベイスターズ","ヤクルトスワローズ","中日ドラゴンズ")

    val arrayPlayOffStage = arrayOf("ファーストステージ　第１戦","ファーストステージ　第２戦","ファーストステージ　第３戦",
        "ファイナルステージ　第１戦","ファイナルステージ　第２戦","ファイナルステージ　第３戦","ファイナルステージ　第４戦",
        "ファイナルステージ　第５戦","ファイナルステージ　第６戦","日本シリーズ　第１戦","日本シリーズ　第２戦","日本シリーズ　第３戦",
        "日本シリーズ　第４戦","日本シリーズ　第５戦","日本シリーズ　第６戦","日本シリーズ　第７戦","日本シリーズ　第８戦")
    //private lateinit var realmDiaryList : RealmDiaryList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDiaryDetailBinding.inflate(layoutInflater)

        realm = Realm.getDefaultInstance()
        setContentView(binding.root)

        binding.apply {

            constLayout.background.alpha = 20
            include.nestview.background.alpha = 20

            //インテントのデータ取り出し

            id = intent.getLongExtra("id", 0)
            vsId = intent.getLongExtra("vsId", 0)
            shareId = intent.getLongExtra("shareId", 0)

            //保存ボタン設定

            include.buttonSave.setOnClickListener {

                AlertSaveDialog().show(supportFragmentManager,"保存")
            }

            //スピナー設定

            var arraySpinner = resources.getStringArray(R.array.arrayGameValue)
            var adapterSpinner = ArrayAdapter<String>(this@DiaryDetailActivity, R.layout.spinner, arraySpinner)
            include.spinnerGameValue.adapter = adapterSpinner

            //RealmDiaryList,RealmDiaryDetail,RealmShareDiaryDetailの取り出し

            realmDiaryList = realm.where<RealmDiaryList>().equalTo("id", id).findFirst()
            realmDiaryDetail = realm.where<RealmDiaryDetail>().equalTo("id", id).findFirst()
            realmShareDiaryDetail = realm.where<RealmShareDiaryDetail>().equalTo("shareId", shareId).findFirst()

            realmTeamGradeFirst = realm.where<realmTeamGrade>().equalTo("id",realmShareDiaryDetail?.firstTeamId).findFirst()
            realmTeamGradeSecond = realm.where<realmTeamGrade>().equalTo("id",realmShareDiaryDetail?.secondTeamId).findFirst()

            realmDiaryListFirst = realm.where<RealmDiaryList>().equalTo("id",realmShareDiaryDetail?.firstTeamId).findFirst()
            realmDiaryListSecond = realm.where<RealmDiaryList>().equalTo("id",realmShareDiaryDetail?.secondTeamId).findFirst()

            realmDiaryList?.run {

                //戦績を表示

                val gameNumber = realm.where<RealmDiaryList>()
                    .equalTo("teamNumber", teamNumber)
                    .equalTo("vsTeamNumber", vsTeamNumber)
                    .equalTo("leagueMode",leagueMode)
                    .lessThanOrEqualTo("id", id)
                    .equalTo("year", year).findAll().size

                val gameWin = realm.where<RealmDiaryList>()
                    .equalTo("teamNumber", teamNumber)
                    .equalTo("vsTeamNumber", vsTeamNumber)
                    .equalTo("leagueMode",leagueMode)
                    .equalTo("gameResult", "◯")
                    .equalTo("year", year).findAll().size

                val gameLose = realm.where<RealmDiaryList>()
                    .equalTo("teamNumber", teamNumber)
                    .equalTo("vsTeamNumber", vsTeamNumber)
                    .equalTo("leagueMode",leagueMode)
                    .equalTo("gameResult", "✕")
                    .equalTo("year", year).findAll().size

                val gameDraw = realm.where<RealmDiaryList>()
                    .equalTo("teamNumber", teamNumber)
                    .equalTo("vsTeamNumber", vsTeamNumber)
                    .equalTo("leagueMode",leagueMode)
                    .equalTo("gameResult", "引")
                    .equalTo("year", year).findAll().size

                textBattleRecord.text =
                    "${arrayTeamName[vsTeamNumber]}戦　${gameNumber}回戦　${gameWin}勝${gameLose}敗${gameDraw}分"

                //リーグモードがクライマックス・日本シリーズならステージを表示

                if(leagueMode > 3)
                    textBattleRecord.text = arrayPlayOffStage[playOffStage]

                //日付を表示

                textDate.text = date

            }

            //先攻のチーム名と得点とホームラン数を表示

            realmTeamGradeFirst?.run{

                textFirstTeamName.text = arrayTeamName[teamNumber]
                editFirstScore.setText(getPoint.toString())
                textFirstTeam.text = arrayTeamName[teamNumber]
                textFirstHomerun.text = "${arrayTeamName[teamNumber]}"
                editFirstHomerun2.setText(homerun.toString())
            }

            //後攻のチーム名と得点とホームラン数を表示

            realmTeamGradeSecond?.run{
                textSecondTeamName.text = arrayTeamName[teamNumber]
                editSecondScore.setText(getPoint.toString())
                textSecondTeam.text = arrayTeamName[teamNumber]
                textSecondHomerun.text = "${arrayTeamName[teamNumber]}"
                editSecondtHomerun2.setText(homerun.toString())
            }

            realmShareDiaryDetail?.run {

                //球場を表示

                editPlace.setText(place)

                //スコアボードを表示

                arrayEditText = arrayOf(editBoard10,editBoard11,editBoard12,editBoard13,
                    editFirst1,editFirst2,editFirst3,editFirst4,editFirst5,editFirst6,editFirst7,editFirst8,editFirst9,editFirst10,editFirst11,editFirst12,editFirst13,
                    editSecond1,editSecond2,editSecond3,editSecond4,editSecond5,editSecond6,editSecond7,editSecond8,editSecond9,editSecond10,editSecond11,editSecond12,editSecond13)

                val arrayScoreBoard = scoreBoard.split(",")
                for (i in 0..29){
                    arrayEditText[i].setText(arrayScoreBoard[i])
                }

                //勝利・敗戦投手を表示

                editWinPitcher.setText(winPitcher)
                editLosePitcher.setText(losePitcher)

                //ホームラン打者を表示

                editFirstHomerun.setText(firstHomerunBatter)
                editSecondHomerun.setText(secondHomerunBatter)
            }

            realmDiaryDetail?.run {

                //タイトルを表示

                include.editTitle.setText(title)

                //ベストプレイヤーを表示

                include.editBestPlayer.setText(bestPlayer)

                //試合評価を表示

                include.spinnerGameValue.setSelection(gameValue)

                //本文を表示

                include.editDiary.setText(diaryBody)

            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //保存ボタンの処理

    override fun onAlertSaveClick() {



        binding.apply {

            //得点とホームラン数が無文字かどうかチェック

            val firstHomerun = editFirstHomerun2.text.toString()
            val secondHomerun = editSecondtHomerun2.text.toString()

            if (editFirstScore.text.toString().isEmpty() || editSecondScore.text.toString().isEmpty() ||
                firstHomerun.isEmpty() || secondHomerun.isEmpty()){
                Snackbar.make(root,"得点かホームラン数が入力されてません",Snackbar.LENGTH_LONG).show()
                return
            }

            //得点とホームラン数に数字以外が入力されてないかチェック

            if (editFirstScore.text.toString().toIntOrNull() == null || editSecondScore.text.toString().toIntOrNull() == null ||
                firstHomerun.toIntOrNull() == null || secondHomerun.toIntOrNull() == null){
                Snackbar.make(root,"得点かホームラン数に数値が入力されてません",Snackbar.LENGTH_LONG).show()
                return
            }

            val firstScore = editFirstScore.text.toString().toInt()
            val secondScore = editSecondScore.text.toString().toInt()
            val editGameScore = makeGameScore(secondScore, firstScore)
            val firstGameResult = if (firstScore > secondScore){"◯"}else if (secondScore > firstScore){"✕"}else "引"
            val secondGameResult = if (firstScore < secondScore){"◯"}else if (secondScore < firstScore){"✕"}else "引"

            realm.executeTransaction{

                realmTeamGradeFirst?.run {

                    getPoint = firstScore
                    lostPoint = secondScore
                    homerun = firstHomerun.toInt()
                }

                realmTeamGradeSecond?.run {

                    getPoint = secondScore
                    lostPoint = firstScore
                    homerun = secondHomerun.toInt()
                }

                realmDiaryListFirst?.run {

                    gameResult = firstGameResult
                    gameScore = editGameScore
                    getPoint = firstScore
                    lostPoint = secondScore
                    homerun = firstHomerun.toInt()
                }

                realmDiaryListSecond?.run {

                    gameResult = secondGameResult
                    gameScore = editGameScore
                    getPoint = secondScore
                    lostPoint = firstScore
                    homerun = secondHomerun.toInt()
                }

                realmDiaryList?.run {

                    title = include.editTitle.text.toString()
                    bestPlayer = include.editBestPlayer.text.toString()
                    gameValue = include.spinnerGameValue.selectedItemPosition
                }

                realmShareDiaryDetail?.run {

                    val stringBuilder = StringBuilder()
                    for (i in 0..28){
                        stringBuilder.append(arrayEditText[i].text.toString())
                        stringBuilder.append(",")
                    }
                    stringBuilder.append(arrayEditText[29].text.toString())

                    place = editPlace.text.toString()
                    scoreBoard = stringBuilder.toString()
                    winPitcher = editWinPitcher.text.toString()
                    losePitcher = editLosePitcher.text.toString()
                    firstHomerunBatter = editFirstHomerun.text.toString()
                    secondHomerunBatter = editSecondHomerun.text.toString()



                }

                realmDiaryDetail?.run {

                    title = include.editTitle.text.toString()
                    bestPlayer = include.editBestPlayer.text.toString()
                    gameValue = include.spinnerGameValue.selectedItemPosition
                    diaryBody = include.editDiary.text.toString()
                }
            }

        }

       // Toast.makeText(this@DiaryDetailActivity, "試合結果を保存しました", Toast.LENGTH_LONG).show()
        Snackbar.make(binding.root,"保存しました",Snackbar.LENGTH_LONG).show()
    }

    //////////////////////////////////////////////////////////////////////////////////

    override fun onDestroy() {
        super.onDestroy()

        realm.isClosed
    }

}