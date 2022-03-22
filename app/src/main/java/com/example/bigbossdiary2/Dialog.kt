package com.example.bigbossdiary2

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

////////////////////////////////////////////////////////////////////////////////////////////
//日付ダイアログ

class DatePickerFragment : DialogFragment(),DatePickerDialog.OnDateSetListener{

    interface OnDatePickListener{
        fun onDateSelect(year : Int ,month : Int,day : Int)
    }
    private  lateinit var listener : OnDatePickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when{
            context is OnDatePickListener -> listener = context
            parentFragment is OnDatePickListener -> listener = parentFragment as OnDatePickListener
            //parentFragment is ClimaxFinalFragment -> listener = parentFragment as ClimaxFinalFragment
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val year = arguments?.getInt("year") ?: 2012

        val calendar = Calendar.getInstance()
        //val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireContext(),this,year,month,day)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        listener.onDateSelect(p1,p2 + 1,p3)
    }

    companion object{

        fun newInstance(year : Int): DialogFragment {

            return  DatePickerFragment().apply {
                arguments = Bundle().apply {
                    putInt("year",year)
                }
            }
        }
    }
}

////////////////////////////////////////////////////////////////////////////////////////
//保存確認ダイアログ

class AlertSaveDialog : DialogFragment(){

    interface  OnAlertSaveListener{
        fun onAlertSaveClick()
    }
    private lateinit var listener : OnAlertSaveListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when{
            parentFragment is OnAlertSaveListener ->
                listener = parentFragment as OnAlertSaveListener

            context is OnAlertSaveListener ->
                listener = context

        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity).apply {
            setTitle("保存確認ダイアログ")
            setMessage("この内容で保存しますか？")
            setPositiveButton("OK"){dialog,which -> listener.onAlertSaveClick()}
            setNegativeButton("キャンセル"){dialog,which ->}
        }
        return builder.create()
    }
}

////////////////////////////////////////////////////////////////////////////////////////
//削除確認ダイアログ

class AlertDeleteDialog : DialogFragment() {

    interface OnAlertDeleteListener {
        fun onAlertDeleteClick(data : Int)
    }
    private lateinit var listener : OnAlertDeleteListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when{
            parentFragment is OnAlertDeleteListener ->
                listener = parentFragment as OnAlertDeleteListener
            context is OnAlertDeleteListener ->
                listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val deleteKey = arguments?.getInt("deleteKey") ?: 2012
        val date = arguments?.getString("date")

        var message = ""
        if (context is ScrollingActivity)
            message  = "$deleteKey 年のデータを全て削除してもよろしいですか？"
        else if (context is DiaryListActivity)
            message = "$date のデータを削除します\n対戦チームの$date のデータも削除しますがよろしいですか？"
        else
            message  = "$deleteKey 年の$date のデータを\n全て削除してもよろしいですか？"

        return AlertDialog.Builder(activity).apply {
            setTitle("削除確認ダイアログ")
            setMessage(message)
            setPositiveButton("はい"){dialog,which -> listener.onAlertDeleteClick(deleteKey)}
            setNegativeButton("キャンセル"){dialog,which ->}
        }.create()

    }

    companion object{

        fun newInstance(deleteKey : Int): DialogFragment {

            return  AlertDeleteDialog().apply {
                arguments = Bundle().apply {
                    putInt("deleteKey",deleteKey)
                }
            }
        }
        fun newInstance(deleteKey : Int, date : String): DialogFragment {

            return  AlertDeleteDialog().apply {
                arguments = Bundle().apply {
                    putInt("deleteKey",deleteKey)
                    putString("date",date)
                }
            }
        }
    }
}