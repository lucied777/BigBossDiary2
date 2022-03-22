package com.example.bigbossdiary2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.bigbossdiary2.databinding.ActivityScrollingBinding
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.content_scrolling.*
import java.util.*

class ScrollingActivity : AppCompatActivity() ,AlertDeleteDialog.OnAlertDeleteListener {

    private lateinit var binding: ActivityScrollingBinding

    private lateinit var realm : Realm

    private var year = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityScrollingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        realm = Realm.getDefaultInstance()

        ///////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////   各種Viewの設定     /////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////

        binding.apply {

            toolbarLayout.title = title
            constLayout.background.alpha = 25
            //include.nestview.background.alpha = 25

            //ピッカーの年数を設定

            pickerYear.apply {
                maxValue = 2050
                minValue = 2010
                year = (Calendar.getInstance()).get(Calendar.YEAR)
                value = if (year < 2051) year else{2050}}

            //ボタンの設定

            buttonPaleague.setOnClickListener(onButtonClickListener)
            buttonSeleague.setOnClickListener(onButtonClickListener)
            buttonInter.setOnClickListener(onButtonClickListener)
            buttonOpen.setOnClickListener(onButtonClickListener)
            buttonPaClimaxFirst.setOnClickListener(onButtonClickListener)
            buttonPaClimaxFinal.setOnClickListener(onButtonClickListener)
            buttonSeClimaxFirst.setOnClickListener(onButtonClickListener)
            buttonSeClimaxFinal.setOnClickListener(onButtonClickListener)
            buttonJapan.setOnClickListener(onButtonClickListener)

            buttonDelete.setOnClickListener {

                year = include.pickerYear.value
                val dialog = AlertDeleteDialog.newInstance(year)
                dialog.show(supportFragmentManager,"削除ボタン")
            }

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //リーグボタンのリスナー

    val onButtonClickListener = object : View.OnClickListener{

        override fun onClick(view: View?) {

            year = binding.include.pickerYear.value

            var leagueMode : Int
            var tagPosition : Int = 0

            when(view?.id){

                R.id.buttonPaleague -> leagueMode = 0 //パリーグ
                R.id.buttonInter -> leagueMode = 1 //交流戦
                R.id.buttonSeleague -> leagueMode = 2 //セリーグ
                R.id.buttonOpen -> leagueMode = 3 //オープン戦

                R.id.buttonPaClimaxFirst -> { leagueMode = 4; tagPosition = 0 } //パリーグCSファーストステージ
                R.id.buttonPaClimaxFinal -> { leagueMode = 4; tagPosition = 1 } //パリーグCSファイナルステージ
                R.id.buttonSeClimaxFirst -> { leagueMode = 5; tagPosition = 0 } //セリーグCSファーストステージ
                R.id.buttonSeClimaxFinal -> { leagueMode = 5; tagPosition = 1 } //セリーグCSファーストステージ
                else -> {leagueMode = 6; tagPosition = 2} //日本シリーズ
            }

            when(leagueMode){
                0,1,2,3 -> {
                    val intent = Intent(this@ScrollingActivity,TeamGradeActivity::class.java)
                    intent.putExtra("leagueMode",leagueMode)
                    intent.putExtra("year",year)
                    startActivity(intent)}
                4 -> {
                    val intent = Intent(this@ScrollingActivity,PaleaguePlayOffActivity::class.java)
                    //intent.putExtra("leagueMode",leagueMode)
                    intent.putExtra("tagPosition",tagPosition)
                    intent.putExtra("year",year)
                    startActivity(intent)}
                5 -> {
                    val intent = Intent(this@ScrollingActivity,SeleaguePlayOffActivity::class.java)
                    //intent.putExtra("leagueMode",leagueMode)
                    intent.putExtra("tagPosition",tagPosition)
                    intent.putExtra("year",year)
                    startActivity(intent)}
                else -> {
                    val intent = Intent(this@ScrollingActivity,JapanSeriesActivity::class.java)
                    //intent.putExtra("leagueMode",leagueMode)
                    intent.putExtra("tagPosition",tagPosition)
                    intent.putExtra("year",year)
                    startActivity(intent)
                }
            }


        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //削除ボタンの処理　※削除ダイアログからの処理

    override fun onAlertDeleteClick(deleteYear : Int) {

        realm.executeTransaction{

            realm.where<realmTeamGrade>().equalTo("year",deleteYear).findAll().deleteAllFromRealm()
            realm.where<realmTeamGradeAppend>().equalTo("year",deleteYear).findAll().deleteAllFromRealm()
            realm.where<RealmDiaryList>().equalTo("year",deleteYear).findAll().deleteAllFromRealm()
            realm.where<RealmDiaryDetail>().equalTo("year",deleteYear).findAll().deleteAllFromRealm()
            realm.where<RealmShareDiaryDetail>().equalTo("year",deleteYear).findAll().deleteAllFromRealm()

            realm.where<RealmPlayOff>().equalTo("year",deleteYear).findAll().deleteAllFromRealm()
        }

        Toast.makeText(this, "$deleteYear 年のデータを全て削除しました", Toast.LENGTH_SHORT).show()

    }

    ///////////////////////////////////////////////////////////////////////////////////////

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