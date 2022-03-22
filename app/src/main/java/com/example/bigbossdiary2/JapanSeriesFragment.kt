package com.example.bigbossdiary2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bigbossdiary2.databinding.FragmentJapanSeriesBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*

class JapanSeriesFragment : Fragment() ,AlertSaveDialog.OnAlertSaveListener,
    DatePickerFragment.OnDatePickListener  {

    private lateinit var  realm : Realm
    private var _binding : FragmentJapanSeriesBinding? = null
    private val binding: FragmentJapanSeriesBinding get() = _binding!!

    private var leagueMode : Int = 0
    private var year : Int = 202
    private var playOffId : Long = 0

    private var month : Int = 1

    private var calendarYear = 2020 //年数チェック用

    private lateinit var date : String
    private lateinit var dateID : String

    private var firstID : Long= 0
    private var secondID : Long= 0
    private var shareID : Long = 0

    private var place = ""
    private var firstTeamNumber = 0
    private var secondTeamNumber = 0
    private var firstGetPoint = 0
    private var secondGetPoint = 0
    private var firstGameResult = "✕"
    private var secondGameResult = "◯"
    private var firstHomerun = 0
    private var secondHomerun = 0
    private var firstBestPlayer = ""
    private var secondBestPlayer = ""
    private var playOffStage = 0

    private var seleagueChampionTeamNumber = 0 //セリーグCSFinal突破チーム
    private var paleagueChampionTeamNumber = 1 //パリーグCSFinal突破チーム

    private lateinit var arrayFirstTextGameResult : Array<TextView> //試合結果表示用
    private lateinit var arraySecondTextGameResult : Array<TextView> //試合結果表示用
    private lateinit var arrayTeamNumber : IntArray //試合結果登録時のスピナーのチームナンバー用

    //スピナー用チーム名配列
    private val arrayTeamName = arrayOf("　日本ハム","　ソフバン","　西武","　楽天","　ロッテ","　オリックス",
        "　巨人","　広島","　阪神","　横浜","　ヤクルト","　中日")

    private val arrayTeamName2 = arrayOf("日本ハム","ソフバン","西武","楽天","ロッテ","オリックス",
        "巨人","広島","阪神","横浜","ヤクルト","中日")

    private val arrayLongTeamName = arrayOf(
        "日本ハムファイターズ","ソフトバンクホークス","西武ライオンズ","楽天イーグルス","千葉ロッテマリーンズ","オリックスバッファローズ",
        "読売ジャイアンツ","広島東洋カープ","阪神タイガース","横浜DeNAベイスターズ","ヤクルトスワローズ","中日ドラゴンズ")

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //onActivityResultが非推奨のためDiaryListActivityへ遷移したときの結果を受け取るインスタンスを作成

    val resultLancher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        //if(it?.resultCode == RESULT_OK){
        //Toast.makeText(this, "ロードしました", Toast.LENGTH_SHORT).show()
        //Snackbar.make(binding.root,"保存しました",Snackbar.LENGTH_LONG).show()
        makeGameResult()

    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            leagueMode = it.getInt("leagueMode")
            year = it.getInt("year")
            playOffId = it.getLong("playOffId")
        }
        realm = Realm.getDefaultInstance()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentJapanSeriesBinding.inflate(inflater,container,false)


        ////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////   各種Viewの設定     /////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////

        binding.apply {

            //日付ボタンに今日の日付を設定

            val calendar = Calendar.getInstance()

            calendarYear = year
            var month = calendar.get(Calendar.MONTH)+1
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            onDateSelect(calendarYear,month,day )

            layoutMain2.background.alpha = 20

            //試合結果を表示するテキストビューの配列を作成

            arrayFirstTextGameResult = arrayOf(textFirstGameResult1,textFirstGameResult2,textFirstGameResult3,
                textFirstGameResult4,textFirstGameResult5,textFirstGameResult6,textFirstGameResult7)
            arraySecondTextGameResult = arrayOf(textSecondGameResult1,textSecondGameResult2,textSecondGameResult3,
                textSecondGameResult4,textSecondGameResult5,textSecondGameResult6,textFirstGameResult7)

            //RealmPlayOffTeamにファーストステージのチーム名が登録されてるかチェック

            checkRealmPlayOff()

            //スピナー設定

            spinnerFirstTeam.onItemSelectedListener = listenerSpinner
            spinnerSecondTeam.onItemSelectedListener = listenerSpinner

            spinnerFirstTeam.isFocusable = false
            spinnerSecondTeam.isFocusable = false

            ///////////////////////////////////////
            //ボタン設定

            //日付ボタン
            buttonhiduke.setOnClickListener {  DatePickerFragment.newInstance(year).show(childFragmentManager,"日付ボタン") }

            //保存ボタン
            buttonSave.setOnClickListener { AlertSaveDialog().show(childFragmentManager,"保存ボタン") }

            //クリアボタン
            buttonClear.setOnClickListener { clear() }

            //CSファーストステージ突破チームボタン
            buttonFirstTeam.setOnClickListener {

                val intent = Intent(context,DiaryListActivity::class.java)
                intent.putExtra("teamNumber",seleagueChampionTeamNumber)
                intent.putExtra("year",year)
                intent.putExtra("leagueMode",leagueMode)

                //DiaryListActivityでデータを削除し ClimaxFirstFragment に戻ってきた時
                //それを試合結果表に反映させるためにlaunchメソッドで遷移する

                this@JapanSeriesFragment.resultLancher.launch(intent)
            }
            //リーグ優勝チームボタン
            buttonSecondTeam.setOnClickListener {

                val intent = Intent(context,DiaryListActivity::class.java)
                intent.putExtra("teamNumber",paleagueChampionTeamNumber)
                intent.putExtra("year",year)
                intent.putExtra("leagueMode",leagueMode)

                //DiaryListActivityでデータを削除し ClimaxFirstFragment に戻ってきた時
                //それを試合結果表に反映させるためにlaunchメソッドで遷移する

                this@JapanSeriesFragment.resultLancher.launch(intent)
            }

            return root
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //RealmPlayOffTeamにファイナルステージのチーム名が登録されてるかチェックするメソッド

    fun checkRealmPlayOff(){

        binding.apply {

            val arraySpinner = resources.getStringArray(R.array.arrayJapan)
            var spinnerAdapter = ArrayAdapter<String>(requireContext(),R.layout.spinner,arraySpinner)
            spinnerPlayOff.adapter = spinnerAdapter

            val realmPlayOff =
                realm.where<RealmPlayOff>().equalTo("playOffId", playOffId).findFirst()

            //データがあるならスピナーの作成とボタンのチーム名表示

            realmPlayOff?.let {

                seleagueChampionTeamNumber = it.firstTeamNumber //CSファーストステージ突破チーム
                paleagueChampionTeamNumber = it.secondTeamNumber //リーグ優勝チーム

                //スピナーを作成

                arrayTeamNumber = intArrayOf(it.firstTeamNumber,it.secondTeamNumber)

                val listTeamName = listOf<String>(arrayTeamName[it.firstTeamNumber],arrayTeamName[it.secondTeamNumber])
                spinnerAdapter = ArrayAdapter<String>(requireContext(),R.layout.spinner,listTeamName)

                spinnerFirstTeam.apply {
                    adapter = spinnerAdapter
                    setSelection(0)
                    isEnabled = true
                }
                spinnerSecondTeam.apply {
                    adapter = spinnerAdapter
                    setSelection(1)
                    isEnabled = true
                }

                //チーム名表示

                buttonFirstTeam.text = arrayTeamName2[it.firstTeamNumber]
                buttonSecondTeam.text = arrayTeamName2[it.secondTeamNumber]

                //試合結果表示

                makeGameResult()

                return
            }

            //データがないならスピナーと保存ボタンを使用不可

            unEnable()

            buttonFirstTeam.isEnabled = false
            buttonFirstTeam.setBackgroundResource(R.drawable.bottontype2)
            buttonSecondTeam.isEnabled = false
            buttonSecondTeam.setBackgroundResource(R.drawable.bottontype2)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //試合結果を表示するメソッド

    fun makeGameResult(){

        for (firstTextGameResult in arrayFirstTextGameResult){
            firstTextGameResult.text = ""
        }
        for (secondTextGameResult in arraySecondTextGameResult){
            secondTextGameResult.text = ""
        }

        //CSファーストステージ突破チーム試合結果表示

        val listFirstRealmDiaryList = realm.where<RealmDiaryList>()
            .equalTo("year",year)
            .equalTo("teamNumber",seleagueChampionTeamNumber)
            .equalTo("leagueMode",leagueMode).findAll().sort("playOffStage")

        for (firstRealmDiaryList in listFirstRealmDiaryList){
            arrayFirstTextGameResult[firstRealmDiaryList.playOffStage - 9].text = firstRealmDiaryList.gameResult

            when(firstRealmDiaryList.gameResult){
                "◯" -> arrayFirstTextGameResult[firstRealmDiaryList.playOffStage - 9].setTextColor(Color.RED)
                "✕" -> arrayFirstTextGameResult[firstRealmDiaryList.playOffStage - 9].setTextColor(Color.BLUE)
                "引" -> arrayFirstTextGameResult[firstRealmDiaryList.playOffStage - 9].setTextColor(Color.GREEN)
            }
        }

        //リーグ優勝チーム試合結果表示

        val listSecondRealmDiaryList = realm.where<RealmDiaryList>()
            .equalTo("year",year)
            .equalTo("teamNumber",paleagueChampionTeamNumber)
            .equalTo("leagueMode",leagueMode).findAll().sort("playOffStage")

        for (secondRealmDiaryList in listSecondRealmDiaryList){
            arraySecondTextGameResult[secondRealmDiaryList.playOffStage - 9].text = secondRealmDiaryList.gameResult

            when(secondRealmDiaryList.gameResult){
                "◯" -> arraySecondTextGameResult[secondRealmDiaryList.playOffStage - 9].setTextColor(Color.RED)
                "✕" -> arraySecondTextGameResult[secondRealmDiaryList.playOffStage - 9].setTextColor(Color.BLUE)
                "引" -> arraySecondTextGameResult[secondRealmDiaryList.playOffStage - 9].setTextColor(Color.GREEN)
            }
        }
        makeFinalJudge()
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //ファイナルステージの勝敗判定

    fun makeFinalJudge(){

        binding.apply {

            //CSファーストステージ突破チームの勝ち数と引き分け数

            val firstWin = realm.where<RealmDiaryList>()
                .equalTo("year",year)
                .equalTo("leagueMode",leagueMode)
                .equalTo("teamNumber",seleagueChampionTeamNumber)
                .equalTo("gameResult","◯").findAll().size

            val firstDraw = realm.where<RealmDiaryList>()
                .equalTo("year",year)
                .equalTo("leagueMode",leagueMode)
                .equalTo("teamNumber",seleagueChampionTeamNumber)
                .equalTo("gameResult","引").findAll().size

            //リーグ優勝チームの勝ち数と引き分け数

            val secondWin = realm.where<RealmDiaryList>()
                .equalTo("year",year)
                .equalTo("leagueMode",leagueMode)
                .equalTo("teamNumber",paleagueChampionTeamNumber)
                .equalTo("gameResult","◯").findAll().size

            val secondDraw = realm.where<RealmDiaryList>()
                .equalTo("year",year)
                .equalTo("leagueMode",leagueMode)
                .equalTo("teamNumber",paleagueChampionTeamNumber)
                .equalTo("gameResult","引").findAll().size

            //CSファーストステージ突破チームの判定

            if (firstWin >= 4 || (firstWin >=3 && firstDraw >= 2)
                || (firstWin >=2 && firstDraw >= 4) || (firstWin >=1 && firstDraw >= 6)){
                textWinner.text = "${arrayLongTeamName[seleagueChampionTeamNumber]}"
                textWinner.setTextColor(Color.RED)
                textWinner2.text = "$year ペナントレース日本一！"
                textWinner2.setTextColor(Color.RED)

                unEnable()
                return
            }

            //リーグ優勝チームの判定

            if (secondWin >= 4 || (secondWin >=3 && secondDraw >= 2)
                || (secondWin >=2 && secondDraw >= 4) || (secondWin >=1 && secondDraw >= 6) ){
                textWinner.text = "${arrayLongTeamName[paleagueChampionTeamNumber]}"
                textWinner.setTextColor(Color.RED)
                textWinner2.text = "$year ペナントレース日本一！"
                textWinner2.setTextColor(Color.RED)

                unEnable()
                return
            }

            textWinner.text = "日本シリーズ"
            textWinner2.text = "天下分け目の最終決戦！"

            enable()

        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //試合登録を使用可にするメソッド

    fun enable(){

        binding.apply {

            spinnerFirstTeam.isEnabled = true
            spinnerSecondTeam.isEnabled = true
            buttonSave.isEnabled = true
            buttonSave.setBackgroundResource(R.drawable.bottontype)
            buttonFirstTeam.isEnabled = true
            buttonFirstTeam.setBackgroundResource(R.drawable.bottontype)
            buttonSecondTeam.isEnabled = true
            buttonSecondTeam.setBackgroundResource(R.drawable.bottontype)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //試合登録を使用不可にするメソッド

    fun unEnable(){

        binding.apply {

            spinnerFirstTeam.isEnabled = false
            spinnerSecondTeam.isEnabled = false
            buttonSave.isEnabled = false
            buttonSave.setBackgroundResource(R.drawable.bottontype2)

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //保存ボタンの処理

    override fun onAlertSaveClick() {

        binding.apply {

            //入力値に誤りがないかチェック

            if (editFirstScore.text.toString().length == 0 ||
                editSecondScore.text.toString().length == 0 ||
                editFirstHomerun.text.toString().length == 0 ||
                editSecondHomerun.text.toString().length == 0) {
                Snackbar.make(root,"入力値が間違ってます", Snackbar.LENGTH_LONG).show()
                return
            }

            //設定された年で登録してるかチェック

            if (year !=calendarYear) {
                Snackbar.make(root,"$year 年で作成されていません", Snackbar.LENGTH_LONG).show()
                return
            }

            //先攻後攻のチームナンバーを取得

            firstTeamNumber = arrayTeamNumber[spinnerFirstTeam.selectedItemPosition]
            secondTeamNumber = arrayTeamNumber[spinnerSecondTeam.selectedItemPosition]

            //プレイオフステージを取得

            playOffStage = spinnerPlayOff.selectedItemPosition + 9

            // 先攻後攻と共通のデータベースのプリマリーキーを作成

            firstID = makeID(dateID,firstTeamNumber)
            secondID = makeID(dateID,secondTeamNumber)
            shareID = makeShareID(dateID,firstTeamNumber, secondTeamNumber)

            //登録するデータが既に存在するかチェック

            var realmTeamGrade = realm.where<realmTeamGrade>().equalTo("id",firstID).findFirst()

            if (realmTeamGrade != null) {
                Snackbar.make(root,"データが既に存在します", Snackbar.LENGTH_LONG).show()
                return
            }

            realmTeamGrade = realm.where<realmTeamGrade>().equalTo("id",secondID).findFirst()

            if (realmTeamGrade != null) {
                Snackbar.make(root,"データが既に存在します", Snackbar.LENGTH_LONG).show()
                return
            }

            //CSファーストステージデータが既に存在するかチェック

            realmTeamGrade = realm.where<realmTeamGrade>().equalTo("year",year)
                .equalTo("playOffStage",playOffStage)
                .equalTo("leagueMode",leagueMode).findFirst()

            if (realmTeamGrade != null) {
                Snackbar.make(root,"データが既に存在します", Snackbar.LENGTH_LONG).show()
                return
            }

            //場所取得

            place = editPlace.text.toString()

            // 先攻後攻の得点を取得

            firstGetPoint = editFirstScore.text.toString().toInt()
            secondGetPoint = editSecondScore.text.toString().toInt()

            //先攻後攻の試合結果を取得

            firstGameResult =
                if (secondGetPoint > firstGetPoint){
                    textFirstResult.setTextColor(Color.BLUE)
                    textFirstResult.text = "✕"
                    "✕"
                }else if (firstGetPoint > secondGetPoint){
                    textFirstResult.setTextColor(Color.RED)
                    textFirstResult.text = "◯"
                    "◯"
                }else{
                    textFirstResult.setTextColor(Color.GREEN)
                    textFirstResult.text = "引"
                    "引"
                }

            secondGameResult =
                if (secondGetPoint > firstGetPoint){
                    textSecondResult.setTextColor(Color.RED)
                    textSecondResult.text = "◯"
                    "◯"
                } else if(firstGetPoint > secondGetPoint){
                    textSecondResult.setTextColor(Color.BLUE)
                    textSecondResult.text = "✕"
                    "✕"
                }else{
                    textSecondResult.setTextColor(Color.GREEN)
                    textSecondResult.text = "引"
                    "引"
                }

            //先攻後攻のホームラン数を取得

            firstHomerun = editFirstHomerun.text.toString().toInt()
            secondHomerun = editSecondHomerun.text.toString().toInt()

            //先攻後攻のベストプレイヤーを取得

            firstBestPlayer = editFirstBestPlayer.text.toString()
            secondBestPlayer = editSecondBestPlayer.text.toString()

            //データベース に登録

            realm.executeTransaction{

                makeRealmTeamGrade()
                makeRealmDiaryList()

                val firstRealmDiaryDetail = realm.createObject<RealmDiaryDetail>(firstID)
                firstRealmDiaryDetail.let {
                    it.bestPlayer = firstBestPlayer
                    it.year = year
                    it.playOffStage = playOffStage
                    it.leagueMode = leagueMode
                }

                val secondRealmDiaryDetail = realm.createObject<RealmDiaryDetail>(secondID)
                secondRealmDiaryDetail.let {
                    it.bestPlayer = secondBestPlayer
                    it.year = year
                    it.playOffStage = playOffStage
                    it.leagueMode = leagueMode
                }

                val realmShareDiaryDetail = realm.createObject<RealmShareDiaryDetail>(shareID)
                realmShareDiaryDetail.let {
                    it.date = date
                    it.place = place
                    it.firstTeamId = firstID
                    it.secondTeamId = secondID
                    it.playOffStage = playOffStage
                    it.leagueMode = leagueMode
                }
            }

            Snackbar.make(root,"保存しました",Snackbar.LENGTH_LONG).show()

            makeGameResult()
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //realmTeamGradeに登録する処理

    fun makeRealmTeamGrade(){

        //先行チーム
        val firstRealmTeamGrade = realm.createObject<realmTeamGrade>(firstID)
        firstRealmTeamGrade.let {
            it.teamNumber = firstTeamNumber
            it.gameResult = firstGameResult
            it.getPoint = firstGetPoint
            it.lostPoint = secondGetPoint
            it.homerun = firstHomerun
            it.playOffStage = playOffStage
            it.leagueMode = leagueMode
            it.year = year
        }

        //後攻チーム
        val secondRealmTeamGrade = realm.createObject<realmTeamGrade>(secondID)
        secondRealmTeamGrade.let {
            it.teamNumber = secondTeamNumber
            it.gameResult = secondGameResult
            it.getPoint = secondGetPoint
            it.lostPoint = firstGetPoint
            it.homerun = secondHomerun
            it.playOffStage = playOffStage
            it.leagueMode = leagueMode
            it.year = year
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //RealmDiaryListに登録する処理

    fun makeRealmDiaryList(){

        //先行チーム
        val firstRealmDiaryList = realm.createObject<RealmDiaryList>(firstID)
        firstRealmDiaryList.let {
            it.teamNumber = firstTeamNumber
            it.date = date
            it.gameResult  = firstGameResult
            it.secondTeamNumber = secondTeamNumber
            it.gameScore = makeGameScore(secondGetPoint,firstGetPoint)
            it.firstTeamNumber = firstTeamNumber
            it.homerun = firstHomerun
            it.bestPlayer = firstBestPlayer
            it.vsTeamNumber = secondTeamNumber
            it.vsId = secondID
            it.getPoint = firstGetPoint
            it.lostPoint = secondGetPoint
            it.shareId = shareID
            it.playOffStage = playOffStage
            it.month = month
            it.year = year
            it.leagueMode = leagueMode
        }

        //後攻チーム
        val secondRealmDiaryList = realm.createObject<RealmDiaryList>(secondID)
        secondRealmDiaryList.let {
            it.teamNumber = secondTeamNumber
            it.date = date
            it.gameResult  = secondGameResult
            it.secondTeamNumber = secondTeamNumber
            it.gameScore = makeGameScore(secondGetPoint,firstGetPoint)
            it.firstTeamNumber = firstTeamNumber
            it.homerun = secondHomerun
            it.bestPlayer = secondBestPlayer
            it.vsTeamNumber = firstTeamNumber
            it.vsId = firstID
            it.getPoint = secondGetPoint
            it.lostPoint = firstGetPoint
            it.shareId = shareID
            it.playOffStage = playOffStage
            it.month = month
            it.year = year
            it.leagueMode = leagueMode
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //日付ボタンを押したときの処理　※　DatePickerFragmentの処理　

    override fun onDateSelect(year: Int, month: Int, day: Int) {

        this.month = month
        calendarYear = year
        dateID = String.format("%d%02d%02d",year,month,day)
        date = String.format("%d年%d月%d日",year,month,day)
        binding.buttonhiduke.text = date
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //クリアメソッド

    fun clear(){

        binding.apply {
            editFirstScore.setText("0")
            editSecondScore.setText("0")
            editFirstHomerun.setText("0")
            editSecondHomerun.setText("0")
            editFirstBestPlayer.setText("")
            editSecondBestPlayer.setText("")
            textFirstResult.text = ""
            textSecondResult.text = ""
            editPlace.setText("")
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //spinnerのリスナー ※無名クラス

    val listenerSpinner = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            binding.apply {

                //初期起動時のみリスナーを無効
                if (!spinnerFirstTeam.isFocusable){
                    spinnerFirstTeam.isFocusable = true
                    return
                }
                if (!spinnerSecondTeam.isFocusable){
                    spinnerSecondTeam.isFocusable = true
                    return
                }

                //スピナーで選んだチームで登録できるか判定

                var spinnerFirstPossition = spinnerFirstTeam.selectedItemPosition
                var spinnerSecondPossition = spinnerSecondTeam.selectedItemPosition

                if (spinnerFirstPossition == spinnerSecondPossition){
                    buttonSave.isEnabled = false
                    buttonSave.setBackgroundResource(R.drawable.bottontype2)
                }else{
                    buttonSave.isEnabled = true
                    buttonSave.setBackgroundResource(R.drawable.bottontype)
                }
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////

    companion object {

        @JvmStatic
        fun newInstance(leagueMode: Int, year: Int, playOffId : Long) =
            JapanSeriesFragment().apply {
                arguments = Bundle().apply {
                    putInt("year", year)
                    putInt("leagueMode", leagueMode)
                    putLong("playOffId", playOffId)
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