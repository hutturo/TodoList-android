package com.example.todolist_androidproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import java.util.*

class EditActivity : AppCompatActivity() {

    val realm = Realm.getDefaultInstance() //인스턴스 얻기
    val calendar: Calendar = Calendar.getInstance() //날짜를 다룰 캘린더 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // 업데이트 조건
        val id = intent.getLongExtra("id", -1)
        if (id == -1L) {
            insertMode()
        } else {
            updateMode(id)
        }

        // 캘린더뷰의 날짜를 선택했을 때 Calendar 객체에 설정
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
    }

    // 추가 모드 초기화
    private fun insertMode() {
        // 삭제 버튼 감추기
        deleteFab.hide()

        // 완료 버튼을 클릭하면 추가
        fab.setOnClickListener {
            insertTodo()
        }
    }

    // 수정 모드 초기화
    private fun updateMode(id: Long) {
        // id에 해당하는 객체를 화면에 표시
        realm.where<Todo>().equalTo("id", id).findFirst()?.apply {
            todoEditText.setText(title)
            calendarView.date = date
        }

        // 완료 버튼 클릭시 수정
        fab.setOnClickListener {
            updateTodo(id)
        }

        // 삭제 버튼 클릭시 삭제
        deleteFab.setOnClickListener {
            deleteTodo(id)
        }
    }

    private fun insertTodo() {
        realm.beginTransaction()

        val newItem = realm.createObject<Todo>(nextId())

        newItem.title = todoEditText.text.toString()
        newItem.date = calendar.timeInMillis

        realm.commitTransaction()

        alert("내용이 추가되었습니다.") {
            yesButton{ finish() }
        }.show()
    }

    private fun updateTodo(id: Long) {
        realm.beginTransaction()

        val updateItem = realm.where<Todo>().equalTo("id", id).findFirst()!!

        updateItem.title = todoEditText.text.toString()
        updateItem.date = calendar.timeInMillis

        realm.commitTransaction()

        alert("내용이 변경되었습니다.") {
            yesButton { finish() }
        }.show()
    }



    private fun deleteTodo(id: Long) {
        realm.beginTransaction()

        val deleteItem = realm.where<Todo>().equalTo("id", id).findFirst()!!

        deleteItem.deleteFromRealm()
        realm.commitTransaction()

        alert("내용이 삭제되었습니다.") {
            yesButton{ finish() }
        }.show()
    }

    private fun nextId(): Int {
        val maxId = realm.where<Todo>().max("id")
        if (maxId != null) {
            return maxId.toInt() + 1
        }

        return 0
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
