package com.example.bigbossdiary2

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bigbossdiary2.databinding.RowTeamGradeBinding
import java.time.Year


class TeamGradeAdapter(val leagueMode : Int, val year: Int) : RecyclerView.Adapter<TeamGradeAdapter.ViewHolder>(){

    var listTeamGrade : List<TeamGrade> = ArrayList<TeamGrade>()
    var swichScore = true
    var swichWinRate = true

    private val arrayTeamName = arrayOf("日ハム　","ＳＢ　　","西武　　","楽天　　","ロッテ　","オリ　　","巨人　　","広島　　","阪神　　","DeNA","ヤク　　","中日　　","試合無し")

    private lateinit var onLongClickRecycleListener : (TeamGrade)->Unit

///////////////////////////////////////////////////////////////////////////////////////////
    //プロパティ（リスナー）セッターメソッド

    fun setOnClickRecycleListener ( longClickListener : (TeamGrade)->Unit){

        onLongClickRecycleListener = longClickListener
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //リサイクルビュー高速設定１ ※初期化

    init {
        setHasStableIds(true)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //リサイクルビュー高速設定２

    override fun getItemId(position: Int): Long {
        return (listTeamGrade[position].teamNumber).toLong()
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //プロパティ（swichScore、swichWinRate）セッターメソッド　※ゲーム差・勝率、得点・失点切替えに使用

    fun setList(swichScore : Boolean,swichWinRate : Boolean){
        this.swichScore = swichScore
        this.swichWinRate = swichWinRate
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //プロパティ（listTeamGrade）セッターメソッド　※リストデータ切替えに使用

    fun setList( list : List<TeamGrade>,swichScore : Boolean,swichWinRate : Boolean){
        listTeamGrade = list
        this.swichScore = swichScore
        this.swichWinRate = swichWinRate
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //ビューホルダー

    inner class ViewHolder(val binding: RowTeamGradeBinding) : RecyclerView.ViewHolder(binding.root)

    /////////////////////////////////////////////////////////////////////////////////////////////
    //onCreateViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RowTeamGradeBinding.inflate(layoutInflater,parent,false)

        val holder = ViewHolder(binding)

        //短くリサイクルビューをタップした時のリスナーをセット

        holder.binding.layoutRow.setOnClickListener{
            val position = holder.adapterPosition
            val teamNumber = listTeamGrade.get(position).teamNumber

            val intent = Intent(parent.context,DiaryListActivity::class.java)
            intent.putExtra("teamNumber",teamNumber)
            intent.putExtra("year",year)
            intent.putExtra("leagueMode",leagueMode)

            //DiaryListActivityでデータを削除しTeamGradeActivityに戻ってきた時
            //それをチーム成績リサイクルビューに反映させるためにlaunchメソッドで遷移する

            (parent.context as TeamGradeActivity).resultLancher.launch(intent)
           // onShortClickRecycleListener(listTeamGrade[position].teamNumber)
        }

        //長くリサイクルビューをタップした時のリスナーをセット

        holder.binding.layoutRow.setOnLongClickListener{
            val position = holder.adapterPosition
            onLongClickRecycleListener(listTeamGrade[position])
            return@setOnLongClickListener true
        }
        return holder
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //onBindViewHolder

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            rank.text = String.format("%2d位",listTeamGrade[position].rank)
            teamName.text = arrayTeamName[listTeamGrade[position].teamNumber]
            totalGame.text = String.format("%3d",listTeamGrade[position].totalGame)
            totalWin.text = String.format("%3d",listTeamGrade[position].totalWin)
            totalLose.text = String.format("%3d",listTeamGrade[position].totalLose)
            totalDraw.text = String.format("%3d",listTeamGrade[position].totalDraw)

            //得点・失点切替え
            if (swichScore)totalScore.text = String.format("%3d",listTeamGrade[position].totalGetPoint)
            else totalScore.text = String.format("%3d",listTeamGrade[position].totalLostPoint)

            totalHomerun.text = String.format("%3d",listTeamGrade[position].totalHomerun)

            //勝率・ゲーム差切替え
            if (swichWinRate) winRate.text = String.format("%.1f",listTeamGrade[position].gameDifference)
            else winRate.text = String.format("%.3f",listTeamGrade[position].winRate)
        } //}
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //

    override fun getItemCount(): Int {
        return listTeamGrade.size
    }
}


