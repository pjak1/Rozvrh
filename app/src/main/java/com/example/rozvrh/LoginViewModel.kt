import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rozvrh.api.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiService(application)

    // Login function
    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            apiService.initiateLogin(
                username,
                password,
                onSuccess = {
                    // Switch to Main context to call onSuccess
                    onSuccess() // Call onSuccess on the Main thread
                },
                onError = { errorCode ->
                    // Handle specific error codes
                    Log.e("LoginViewModel", "Error: $errorCode")
                },
                onFailure = { exception ->
                    // Handle network failure
                    Log.e("LoginViewModel", "Network failure", exception)
                }
            )
        }
    }

    // Fetch timetable function
    fun fetchTimeTable() {
        viewModelScope.launch(Dispatchers.IO) {
            apiService.fetchTimeTableData { response ->
                // Ensure this code block is executed in the coroutine context
                response.body?.let { responseBody ->
                    val xmlContent = responseBody.string() // Convert response body to a string
                    saveXmlToFile(xmlContent) // Save XML content to file
                } ?: run {
                    Log.e("LoginViewModel", "Response body is null")
                }
            }
        }
    }

    // Save XML content to a file
    private fun saveXmlToFile(xmlContent: String) {
        // Define the file name
        val fileName = "timetable.xml"

        // Get the file directory within the app's internal storage
        val file = File(getApplication<Application>().filesDir, fileName)

        try {
            // Write the XML content to the file
            file.writeText(xmlContent)
            Log.d("file_save", "XML file saved successfully at: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e("file_save", "Error saving XML file", e)
        }
    }
}
