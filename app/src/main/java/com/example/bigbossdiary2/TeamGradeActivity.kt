package com.example.bigbossdiary2

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.bigbossdiary2.databinding.ActivityTeamGradeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_scrolling.view.*
import kotlinx.android.synthetic.main.row.*
import java.util.*

class TeamGradeActivity : AppCompatActivity(),
    DatePickerFragment.OnDatePickListener ,AlertSaveDialog.OnAlertSaveListener,
    InterleagueFragment.OnInterEditSaveListener{

    private lateinit var binding: ActivityTeamGradeBinding
    private lateinit var viewPager2Adapter: ViewPager2Adapter

    lateinit var realm : Realm

    private var swich = false //ページの切り替え以外に試合結果登録時にもViewpagerのリスナーが起動するため、
                              // スピナーのポジションを調整するためのスイッチ
    private var spinnerFirstPosition= 0 //登録時のspinnerFirstTeamの位置を保持する変数
    private var spinnerSecondPosition = 0 //登録時のspinnerSecondTeamの位置を保持する変数

    private var leagueMode : Int = 0

    private var month : Int = 1
    private var year : Int = 202
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //onActivityResultが非推奨のためDiaryListActivityへ遷移したときの結果を受け取るインスタンスを作成

    val resultLancher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        //if(it?.resultCode == RESULT_OK){
        Toast.makeText(this, "ロードしました", Toast.LENGTH_SHORT).show()
            //Snackbar.make(binding.root,"保存しました",Snackbar.LENGTH_LONG).show()

        makeViewPager2Adapter()
        //}
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTeamGradeBinding.inflate(layoutInflater)
        realm = Realm.getDefaultInstance()

        setContentView(binding.root)
        //setSupportActionBar(binding.toolbar)
        //findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title

        year = intent.getIntExtra("year",2020)
        leagueMode = intent.getIntExtra("leagueMode",0)

        //追加データ、ReamlTeamDataAppendを作成

        makeReamlTeamDataAppend()

        //日付ボタンに今日の日付を設定

        val calendar = Calendar.getInstance()

        calendarYear = year
        var month = calendar.get(Calendar.MONTH)+1
        var day = calendar.get(Calendar.DAY_OF_MONTH)

        onDateSelect(calendarYear,month,day )

        ////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////   各種Viewの設定     /////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////

        binding.apply {

            constLayout.background.alpha = 20
            layoutMain.background.alpha = 20

            //スピナー設定

            spinnerFirstTeam.onItemSelectedListener = listenerSpinner
            spinnerSecondTeam.onItemSelectedListener = listenerSpinner

            //ViewPager2とタブレイアウト設定

            binding.apply {

                var arrayFragment = mutableListOf(

                    PaleagueFragment.newInstance(year),
                    InterleagueFragment.newInstance(year),
                    SeleagueFragment.newInstance(year),
                    OpenGameFragment.newInstance(year))

                viewPager2Adapter = ViewPager2Adapter(this@TeamGradeActivity,arrayFragment)

                viewPager2.adapter = viewPager2Adapter
                viewPager2.registerOnPageChangeCallback(listenerViewPager)
                viewPager2.setCurrentItem(leagueMode)

                //タブレイアウトとviewPager2を関連付け

                val array = arrayOf("パリーグ","交流戦","セリーグ","オープン戦")
                TabLayoutMediator(tabLayout,viewPager2, {tab,position -> tab.text = array[position]}).attach()
            }

            //各ボタン設定

            //日付ボタン
            buttonhiduke.setOnClickListener {
                DatePickerFragment.newInstance(year).show(supportFragmentManager,"日付ボタン")
            }

            //保存ボタン
            buttonSave.setOnClickListener {
                AlertSaveDialog().show(supportFragmentManager,"保存ボタン")
            }

            //クリアボタン
            buttonClear.setOnClickListener {
                clear()
            }
        }
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
    //保存ボタンの処理　※アラート保存ダイアログからの処理

    override fun onAlertSaveClick() {

        binding.apply {

            //入力値に誤りがないかチェック

            if (editFirstScore.text.toString().length == 0 ||
                editSecondScore.text.toString().length == 0 ||
                editFirstHomerun.text.toString().length == 0 ||
                editSecondHomerun.text.toString().length == 0) {
                    Toast.makeText(this@TeamGradeActivity, "入力値が間違ってます", Toast.LENGTH_SHORT).show()
                return
            }

            //設定された年で登録してるかチェック

            if (year !=calendarYear) {
                Toast.makeText(this@TeamGradeActivity, "$year 年で作成されていません", Toast.LENGTH_SHORT).show()
                return
            }

            //先攻後攻のチームナンバーを取得

            firstTeamNumber = spinnerFirstTeam.selectedItemPosition
            secondTeamNumber = spinnerSecondTeam.selectedItemPosition

            if (leagueMode == 2){ //セリーグモードの時は＋６
                firstTeamNumber += 6
                secondTeamNumber += 6
            }

            // 先攻後攻と共通のデータベースのプリマリーキーを作成

            firstID = makeID(dateID,firstTeamNumber)
            secondID = makeID(dateID,secondTeamNumber)
            shareID = makeShareID(dateID,firstTeamNumber, secondTeamNumber)

            //登録するデータが既に存在するかチェック

            val firstTeamGrade = realm.where<realmTeamGrade>().equalTo("id",firstID).findFirst()

            if (firstTeamGrade != null) {
                Toast.makeText(this@TeamGradeActivity, "データが既に存在します", Toast.LENGTH_LONG).show()
                return
            }

            val secondTeamGrade = realm.where<realmTeamGrade>().equalTo("id",secondID).findFirst()

            if (secondTeamGrade != null) {
                Toast.makeText(this@TeamGradeActivity, "データが既に存在します", Toast.LENGTH_LONG).show()
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
                }

                val secondRealmDiaryDetail = realm.createObject<RealmDiaryDetail>(secondID)
                secondRealmDiaryDetail.let {
                    it.bestPlayer = secondBestPlayer
                    it.year = year
                }

                val realmShareDiaryDetail = realm.createObject<RealmShareDiaryDetail>(shareID)
                realmShareDiaryDetail.let {
                    it.date = date
                    it.place = place
                    it.firstTeamId = firstID
                    it.secondTeamId = secondID

                }
            }

            //現在のスピナーの位置を取得
            swich = true
            spinnerFirstPosition = spinnerFirstTeam.selectedItemPosition
            spinnerSecondPosition = spinnerSecondTeam.selectedItemPosition

            makeViewPager2Adapter()

            Toast.makeText(this@TeamGradeActivity, "試合結果を保存しました", Toast.LENGTH_LONG).show()
            // Snackbar.make(binding.root,"保存しました",Snackbar.LENGTH_LONG).show()
        }
        Snackbar.make(binding.root,"保存しました",Snackbar.LENGTH_LONG).show()
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
            it.month = month
            it.year = year
            it.leagueMode = leagueMode
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////  タブレイアウトとveiwPager2の設定   ///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////
    //ViewPager2Adapterを作成

    fun makeViewPager2Adapter(){ //※試合結果登録時にも呼び出される

        binding.apply {

            var arrayFragment = mutableListOf(

                PaleagueFragment.newInstance(year),
                InterleagueFragment.newInstance(year),
                SeleagueFragment.newInstance(year),
                OpenGameFragment.newInstance(year))

            viewPager2Adapter = ViewPager2Adapter(this@TeamGradeActivity,arrayFragment)
            viewPager2.apply {
                adapter = viewPager2Adapter
                setCurrentItem(leagueMode)
            }

        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //ViewPager2のアダプター　※インナークラス

    class ViewPager2Adapter(fragmentActivity: FragmentActivity, var listFragment : List<Fragment>) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return listFragment.size
        }

        override fun createFragment(position: Int): Fragment {
            return  listFragment.get(position)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //ViewPager2のページをめくった時のリスナー　※無名クラス

    val listenerViewPager = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            leagueMode = position

            //スピナーのチーム羅列を決定してアダプターを作成

            var arraySpinner = when(leagueMode){
                0 -> resources.getStringArray(R.array.arrayPaleague) //パリーグ
                1,3 -> resources.getStringArray(R.array.arrayInter) //交流戦
                else -> resources.getStringArray(R.array.arraySeleague) //セリーグ
            }

            var adapterSpinner = ArrayAdapter<String>(this@TeamGradeActivity,R.layout.spinner,arraySpinner)

            binding.apply {

                //スピナーのアダプターをセット

                spinnerFirstTeam.adapter = adapterSpinner
                spinnerSecondTeam.adapter = adapterSpinner

                when(leagueMode){
                    0,2 -> {
                        if (swich){
                            spinnerFirstTeam.setSelection(spinnerFirstPosition)
                            spinnerSecondTeam.setSelection(spinnerSecondPosition)
                        }else{
                            spinnerFirstTeam.setSelection(1)
                            spinnerSecondTeam.setSelection(0)
                            clear()
                        }
                        swich = false
                    }
                    else -> {
                        if (swich){
                            spinnerFirstTeam.setSelection(spinnerFirstPosition)
                            spinnerSecondTeam.setSelection(spinnerSecondPosition)
                        }else{
                            spinnerFirstTeam.setSelection(6)
                            spinnerSecondTeam.setSelection(0)
                            clear()
                        }
                        swich = false
                    }
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //spinnerのリスナー ※無名クラス

    val listenerSpinner = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            binding.apply {

                //スピナーで選んだチームで登録できるか判定

                var spinnerFirstPossition = spinnerFirstTeam.selectedItemPosition
                var spinnerSecondPossition = spinnerSecondTeam.selectedItemPosition

                if (spinnerFirstPossition == spinnerSecondPossition){
                    buttonSave.isEnabled = false
                    buttonSave.setBackgroundResource(R.drawable.bottontype2)
                }else if (leagueMode == 1 && ((spinnerFirstPossition < 6 && spinnerSecondPossition <6)
                            || (spinnerFirstPossition > 5 && spinnerSecondPossition > 5) )){
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
    //その年の追加データ、ReamlTeamDataAppendがなければ作成

    fun makeReamlTeamDataAppend(){
        var realmTeamGradeAppend = realm.where<realmTeamGradeAppend>().equalTo("year",year).findFirst()

        if (realmTeamGradeAppend == null){
            var min : Int
            var max : Int
            for (leaguemode in 0..3){
                when(leaguemode){
                    0 -> {
                        min = 0
                        max = 5
                    }
                    1,3 ->{
                        min = 0
                        max = 11
                    }
                    else ->{
                        min = 6
                        max = 11
                    }
                }

                for (teamnumber in min..max){
                    realm.executeTransaction{
                        realmTeamGradeAppend = realm.createObject<realmTeamGradeAppend>(makeID(year,leaguemode,teamnumber))
                        realmTeamGradeAppend?.teamNumber = teamnumber
                        realmTeamGradeAppend?.leagueMode = leaguemode
                        realmTeamGradeAppend?.year = year
                    }
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //インターリーグフラグメントのエディット保存ボタンが押されたときの処理　
    // ※交流戦でエディットするとセパリーグの順位にも影響が出るので更新する

    override fun onInterEditSaveClick() {
        makeViewPager2Adapter()
        Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show()
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }



}