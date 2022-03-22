package com.example.bigbossdiary2

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bigbossdiary2.databinding.CardLayout3Binding
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class DiaryListAdapter(data : OrderedRealmCollection<RealmDiaryList>, auteUpdate : Boolean)
    : RealmRecyclerViewAdapter<RealmDiaryList, DiaryListAdapter.ViewHolder>(data, auteUpdate){

    inner class ViewHolder(val binding : CardLayout3Binding) : RecyclerView.ViewHolder(binding.root) {

    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //リサイクルビュー高速設定１ ※初期化

    init {
        setHasStableIds(true)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //リサイクルビュー高速設定２

    override fun getItemId(position: Int): Long {
        return data?.get(position)?.id ?:0
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //onCreateViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CardLayout3Binding.inflate(layoutInflater,parent,false)

        //ホルダー作成,
        val holder = ViewHolder(binding)

        //短くリサイクルビューをタップした時のリスナーをセット

        holder.binding.constLayout.setOnClickListener{

            val position = holder.adapterPosition
            val realmDiaryList = data?.get(position)

            if(realmDiaryList != null){

                val intent = Intent(parent.context,DiaryDetailActivity::class.java)
                intent.putExtra("id",realmDiaryList.id)
                intent.putExtra("vsId",realmDiaryList.vsId)
                intent.putExtra("shareId",realmDiaryList.shareId)
                (parent.context as DiaryListActivity).startActivity(intent)
            }
        }

        //長くリサイクルビューをタップした時のリスナーをセット

        holder.binding.constLayout.setOnLongClickListener{

            val position = holder.adapterPosition
            val realmDiaryList = data?.get(position)

            if(realmDiaryList != null){
                var dialog = AlertDeleteDialog.newInstance(realmDiaryList.id.toInt(),realmDiaryList.date)
                dialog.show((parent.context as DiaryListActivity).supportFragmentManager,"削除")
            }
            return@setOnLongClickListener true
        }

        return holder
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //onBindViewHolder

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val arrayTeamName = arrayOf("日本ハム","ソフバン","西武","楽天","ロッテ","オリックス","巨人","広島","阪神","DeNA","ヤクルト","中日")
        val arrayGameValue = arrayOf("","Good","Best!!","Bad","Worst")

        val realmDiaryList = data?.get(position)
        realmDiaryList?.let {
            holder.binding.apply {
                textDate.text = it.date

                when(it.gameValue){
                    1,2 -> textGameValue.setTextColor(Color.RED)
                    3,4 -> textGameValue.setTextColor(Color.BLUE)
                }
                textGameValue.text = arrayGameValue[it.gameValue]

                textTitle.text = it.title
                when(it.gameResult){
                    "◯" -> textGameResult.setTextColor(Color.RED)
                    "✕" -> textGameResult.setTextColor(Color.BLUE)
                    "引" -> textGameResult.setTextColor(Color.GREEN)
                }

                textGameResult.text = it.gameResult
                textSecondTeamName.text = arrayTeamName[it.secondTeamNumber]
                textGameScore.text = it.gameScore
                textFirstTeamName.text = arrayTeamName[it.firstTeamNumber]
                textHomerun.text = it.homerun.toString()
                textBestPlayer.text = it.bestPlayer

            }
        }

    }


}