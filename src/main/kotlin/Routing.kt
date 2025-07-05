package com.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicInteger

@Serializable
data class Task(
    val id: Int,
    val content: String,
    val isDone: Boolean
)

@Serializable
data class TaskRequest(val content: String, val isDone: Boolean)

object TaskRepository{
    private val tasks = mutableListOf<Task>(
        Task(id = 1, content = "Learn Ktor", isDone = true),
        Task(id = 2, content = "Build a REST API", isDone = false),
        Task(id = 3, content = "Write Unit Tests", isDone = false)
    )

    //getAll(),  ดึงข้อมูล tasks ทั้งหมด
    fun getAll(): List<Task>{
        return tasks.toList()
    }
    //getById(id: Int), ดึงข้อมูล tasks by id
    fun getById(id: Int): Task? {
        return tasks.find { it.id == id }
    }
    //add(task: Task),  เพิ่มข้อมูล task เข้าไป
    fun add(task: Task){
        tasks.add(task)
    }
    //update(id: Int, updatedTask: Task),  update ข้อมูล task ตาม id
    fun update(id: Int, updatedTask: Task): Boolean {
        val index = tasks.indexOfFirst { it.id == id }
        return if (index != -1) {
            tasks[index] = updatedTask
            true
        } else {
            false
        }
    }
    //delete(id: Int)   ลบข้อมูล task จาก id
    fun delete(id: Int): Boolean {
        return tasks.removeIf { it.id == id }
    }
}


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello ธนกร กันอูบ")

        }
        // GET /tasks: คืนค่า task ทั้งหมด
        get("/tasks") {
            val tasks = TaskRepository.getAll()
            call.respond(HttpStatusCode.OK, tasks)
        }

        // GET /tasks/{id}: ค้นหาและคืนค่า task เพียงตัวเดียว
        get("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@get
            }

            val task = TaskRepository.getById(id)
            if (task == null) {
                call.respond(HttpStatusCode.NotFound, "Task not found")
            } else {
                call.respond(HttpStatusCode.OK, task)
            }
        }

        // POST /tasks: เพิ่ม task ใหม่
        post("/tasks") {
            val taskRequest = call.receive<TaskRequest>()
            val newTask = Task(
                id = (TaskRepository.getAll().maxOfOrNull { it.id } ?: 0) + 1,
                content = taskRequest.content,
                isDone = taskRequest.isDone
            )
            TaskRepository.add(newTask)
            call.respond(HttpStatusCode.Created, newTask)
        }

        // PUT /tasks/{id}: อัปเดต task ที่มีอยู่
        put("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@put
            }
            val taskRequest = call.receive<TaskRequest>()
            val updatedTask = Task(id, taskRequest.content, taskRequest.isDone)
            val isUpdated = TaskRepository.update(id, updatedTask)
            if (isUpdated) {
                call.respond(HttpStatusCode.OK, updatedTask)
            } else {
                call.respond(HttpStatusCode.NotFound, "Task not found")
            }
        }

        // DELETE /tasks/{id}: ลบ task
        delete("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return@delete
            }
            val isDeleted = TaskRepository.delete(id)
            if (isDeleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Task not found")
            }
        }
    }
}

