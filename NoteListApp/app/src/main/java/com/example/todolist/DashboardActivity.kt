package com.example.todolist

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.DIO.ToDo
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity() {

    lateinit var dbHandler: DBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(dashboard_toolbar)
        title = "Main List"
        dbHandler = DBHandler(this)
        rv_dasboard.layoutManager = LinearLayoutManager(this)

        fab_dasboard.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Add List")
            val view = layoutInflater.inflate(R.layout.dialog_dasboard, null)
            val toDoName = view.findViewById<EditText>(R.id.ev_todo)
            dialog.setView(view)
            dialog.setPositiveButton("Add") { _: DialogInterface, _: Int ->
                if (toDoName.text.isNotEmpty()) {
                    val toDo = ToDo()
                    toDo.name = toDoName.text.toString()
                    dbHandler.addToDo(toDo)
                    refreshList()
                }
            }
            dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

            }
            dialog.show()
        }

    }

    fun updateToDo(toDo: ToDo){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Update List")
        val view = layoutInflater.inflate(R.layout.dialog_dasboard, null)
        val toDoName = view.findViewById<EditText>(R.id.ev_todo)
        toDoName.setText(toDo.name)
        dialog.setView(view)
        dialog.setPositiveButton("Update") { _: DialogInterface, _: Int ->
            if (toDoName.text.isNotEmpty()) {
                toDo.name = toDoName.text.toString()
                dbHandler.updateToDo(toDo)
                refreshList()
            }
        }
        dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

        }
        dialog.show()
    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    private fun refreshList(){
        rv_dasboard.adapter = DashboardAdapter(this,dbHandler.getToDos())
    }


    class DashboardAdapter(val activity: DashboardActivity,val list: MutableList<ToDo>) :
        RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_child_dashboard, p0, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
            holder.toDoName.text = list[p1].name

            holder.toDoName.setOnClickListener {
                val intent = Intent(activity, itemActivity::class.java)
                intent.putExtra(INTENT_TODO_ID,list[p1].id)
                intent.putExtra(INTENT_TODO_NAME,list[p1].name)
                activity.startActivity(intent)
            }

            holder.menu.setOnClickListener {
                val popup = PopupMenu(activity,holder.menu)
                popup.inflate(R.menu.dashboard_child)
                popup.setOnMenuItemClickListener {

                    when(it.itemId){
                        R.id.menu_edit->{
                            activity.updateToDo(list[p1])
                        }
                        R.id.menu_delete->{
                            val dialog = AlertDialog.Builder(activity)
                            dialog.setTitle("Really?")
                            dialog.setMessage("Do you really want delete main list?")
                            dialog.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
                                activity.dbHandler.deleteToDo(list[p1].id)
                                activity.refreshList()
                            }
                            dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

                            }
                            dialog.show()
                        }
                        R.id.menu_mark_as_completed->{
                            activity.dbHandler.updateToDoItemComplatedStatus(list[p1].id,true)
                        }
                        R.id.menu_reset->{
                            activity.dbHandler.updateToDoItemComplatedStatus(list[p1].id,false)
                        }
                    }

                    true
                }
                popup.show()
            }
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val toDoName: TextView = v.findViewById(R.id.tv_todo_name)
            val menu : ImageView = v.findViewById(R.id.iv_menu)
        }
    }
}