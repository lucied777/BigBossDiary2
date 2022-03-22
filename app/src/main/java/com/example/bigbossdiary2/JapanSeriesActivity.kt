package com.example.bigbossdiary2

import android.os.Bundle
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
import com.example.bigbossdiary2.databinding.ActivityJapanSeriesBinding
import com.example.bigbossdiary2.databinding.ActivityPlayOffBinding
import com.google.android.material.tabs.TabLayoutMediator
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where

class JapanSeriesActivity : AppCompatActivity(), AlertSaveDialog.OnAlertSaveListener,
    AlertDeleteDialog.OnAlertDeleteListener{

    private lateinit var binding : ActivityJapanSeriesBinding
    private lateinit var viewPager2Adapter: ViewPager2Adapter
    private lateinit var realm : Realm

    private var leagueMode : Int = 6
    private var year : Int = 202
    private var playOffId : Long = 0

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJapanSeriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setSupportActionBar(findViewById(R.id.toolbar))
        //findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title

        realm = Realm.getDefaultInstance()

        year = intent.getIntExtra("year",2020)

        playOffId = makePlayOffID(year, 6,2)

        ////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////   各種Viewの設定     /////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////

        binding.apply {

            constLayout.background.alpha = 20

            //スピナー設定
            spinnerFirstTeam.onItemSelectedListener = listenerSpinner
            spinnerSecondTeam.onItemSelectedListener = listenerSpinner

            spinnerFirstTeam.isFocusable = false
            spinnerSecondTeam.isFocusable = false

            var arraySpinner =  resources.getStringArray(R.array.arraySeleague) //CSセリーグ、日本シリーズ
            var adapterSpinner = ArrayAdapter<String>(this@JapanSeriesActivity,R.layout.spinner,arraySpinner)

            spinnerFirstTeam.adapter = adapterSpinner

            arraySpinner = resources.getStringArray(R.array.arrayPaleague) //CSパリーグ、日本シリーズ
            adapterSpinner = ArrayAdapter<String>(this@JapanSeriesActivity,R.layout.spinner,arraySpinner)

            spinnerSecondTeam.adapter = adapterSpinner

            spinnerFirstTeam.setSelection(0)
            spinnerSecondTeam.setSelection(0)

            //年数とタイトルとスピナーテキストの表示

            textYear.text = year.toString()

            //////////////////////////////////
            //タブレイアウトとviewPager2設定

            viewPager2.registerOnPageChangeCallback(listenerViewPager)

            makeViewPager2Adapter()

            /////////////////////////
            //ボタン設定

            //保存ボタン
            buttonSave.setOnClickListener { AlertSaveDialog().show(supportFragmentManager,"保存ボタン") }

            //削除ボタン
            buttonDelete.setOnClickListener {
                AlertDeleteDialog.newInstance(year,if (leagueMode == 4){"パリーグ"}else{"セリーグ"})
                    .show(supportFragmentManager,"削除ボタン") }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //ViewPager2Adapterを作成

    fun makeViewPager2Adapter(){ //※RealmPlayOff登録時にも呼び出される

        binding.apply {

            var arrayFragment = mutableListOf(

                //ClimaxFirstFragment.newInstance(4,year,makePlayOffID(year, 4,0)),
                //ClimaxFinalFragment.newInstance(4,year,makePlayOffID(year, 4,1)),
                JapanSeriesFragment.newInstance(6,year,makePlayOffID(year, 6,2)),
                //ClimaxFinalFragment.newInstance(5,year,makePlayOffID(year, 5,1)),
                //ClimaxFirstFragment.newInstance(5,year,makePlayOffID(year, 5,0))
            )

            viewPager2Adapter = ViewPager2Adapter(this@JapanSeriesActivity, arrayFragment)

            viewPager2.apply {

                adapter = viewPager2Adapter

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


            binding.apply {

                checkRealmPlayOff()
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //RealmPlayOffTeamにファーストステージのチーム名が登録されてるかチェックするメソッド

    fun checkRealmPlayOff(){

        binding.apply {

            val realmPlayOff =
                realm.where<RealmPlayOff>().equalTo("playOffId", playOffId).findFirst()

            //データがあるならスピナーと保存ボタンを使用不可にして削除ボタンを使用可にする

            realmPlayOff?.let {

                val spinnerFirstPosition = it.firstTeamNumber -6

                val spinnerSecondPosition = it.secondTeamNumber

                spinnerFirstTeam.setSelection(spinnerFirstPosition)
                spinnerSecondTeam.setSelection(spinnerSecondPosition)
                spinnerFirstTeam.isEnabled = false
                spinnerSecondTeam.isEnabled = false
                buttonSave.isEnabled = false
                buttonSave.setBackgroundResource(R.drawable.bottontype2)
                buttonDelete.isEnabled = true
                buttonDelete.setBackgroundResource(R.drawable.bottontype)

                return
            }

            buttonSave.isEnabled = true
            buttonSave.setBackgroundResource(R.drawable.bottontype)
            buttonDelete.isEnabled = false
            buttonDelete.setBackgroundResource(R.drawable.bottontype2)
            spinnerFirstTeam.isEnabled = true
            spinnerSecondTeam.isEnabled = true
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //保存ボタンの処理

    override fun onAlertSaveClick() {

        binding.apply {

            val spinnerFirstPosition = spinnerFirstTeam.selectedItemPosition + 6

            val spinnerSecondPosition = spinnerSecondTeam.selectedItemPosition

            realm.executeTransaction{

                val realmPlayOff = realm.createObject<RealmPlayOff>(playOffId)
                realmPlayOff.let {
                    it.firstTeamNumber = spinnerFirstPosition
                    it.secondTeamNumber = spinnerSecondPosition
                    it.year = year
                }
            }
        }
        Toast.makeText(this, "保存しました", Toast.LENGTH_LONG).show()

        checkRealmPlayOff()
        makeViewPager2Adapter()
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //削除ボタンの処理　※削除ダイアログからの処理

    override fun onAlertDeleteClick(deleteYear : Int) {

        realm.executeTransaction{

            //日本シリーズ
            realm.where<realmTeamGrade>().equalTo("year",year).equalTo("leagueMode",leagueMode)
                .findAll().deleteAllFromRealm()
            realm.where<RealmDiaryList>().equalTo("year",year).equalTo("leagueMode",leagueMode)
                .findAll().deleteAllFromRealm()
            realm.where<RealmDiaryDetail>().equalTo("year",year).equalTo("leagueMode",leagueMode)
                .findAll().deleteAllFromRealm()
            realm.where<RealmShareDiaryDetail>().equalTo("year",year).equalTo("leagueMode",leagueMode)
                .findAll().deleteAllFromRealm()

            realm.where<RealmPlayOff>().equalTo("playOffId",playOffId).findAll().deleteAllFromRealm()
        }

        Toast.makeText(this, "$deleteYear 年の日本シリーズのデータを全て削除しました", Toast.LENGTH_LONG).show()

        checkRealmPlayOff()
        makeViewPager2Adapter()
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

                buttonSave.isEnabled = true
                buttonSave.setBackgroundResource(R.drawable.bottontype)

            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

}