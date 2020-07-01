import java.util.*
import kotlin.collections.HashMap

class ActivityStatusHandler {
    private var activities: HashMap<String, ActivityTimer> = HashMap()

    fun handleStart(activity: String) {
        val activityTimer = ActivityTimer()
        activityTimer.startTime = Date().time
        activities.put(activity, activityTimer)
    }

    fun handleExit(activity : String) : Long? {
        val activityTimer = activities[activity] ?: return null
        activityTimer.endTime = Date().time
        val time = activityTimer.calculateTime()
        activities.remove(activity)
        return time
    }

    private class ActivityTimer {
        var startTime: Long = 0
        var endTime: Long = 0

        fun calculateTime() : Long {
            return endTime - startTime
        }
    }
}