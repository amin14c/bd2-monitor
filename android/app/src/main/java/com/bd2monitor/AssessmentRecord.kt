package com.bd2monitor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assessments")
data class AssessmentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,
    val type: String,         // "YMRS" أو "MADRS"

    // أسئلة YMRS (0-4 لكل سؤال)
    val ymrs1: Int = 0,       // المزاج المرتفع
    val ymrs2: Int = 0,       // طاقة مفرطة
    val ymrs3: Int = 0,       // كثرة الكلام
    val ymrs4: Int = 0,       // قلة النوم مع خير
    val ymrs5: Int = 0,       // تسارع الأفكار
    val ymrs6: Int = 0,       // تصرفات متهورة
    val ymrs7: Int = 0,       // تضخم الذات

    // أسئلة MADRS (0-4 لكل سؤال)
    val madrs1: Int = 0,      // حزن وفراغ
    val madrs2: Int = 0,      // فقدان الاهتمام
    val madrs3: Int = 0,      // تعب وعدم طاقة
    val madrs4: Int = 0,      // اضطراب النوم
    val madrs5: Int = 0,      // الشعور بالعبء
    val madrs6: Int = 0,      // صعوبة التركيز
    val madrs7: Int = 0,      // أفكار إيذاء النفس

    val ymrsTotal: Int = 0,   // مجموع YMRS (0-28)
    val madrsTotal: Int = 0,  // مجموع MADRS (0-28)

    val timestamp: Long = System.currentTimeMillis()
)
