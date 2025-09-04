import java.util.Date;
import java.util.Calendar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.text.SimpleDateFormat;

/**
 * 日期工具类
 */
public class DateUtils {
    
    /**
     * 方法1：使用Calendar实现（推荐用于Date类型）
     * 将给定日期减1天，并设置为0点0分0秒
     * @param date 原始日期
     * @return 减1天后的0点0分0秒日期
     */
    public static Date subtractOneDayWithCalendar(Date date) {
        if (date == null) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        // 减1天
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        
        // 设置为0点0分0秒0毫秒
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        return calendar.getTime();
    }
    
    /**
     * 方法2：使用LocalDateTime实现（推荐用于Java 8+）
     * 将给定日期减1天，并设置为0点0分0秒
     * @param date 原始日期
     * @return 减1天后的0点0分0秒日期
     */
    public static Date subtractOneDayWithLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        
        // 将Date转换为LocalDateTime
        LocalDateTime localDateTime = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        
        // 减1天并设置为0点0分0秒
        LocalDateTime result = localDateTime
                .minusDays(1)
                .toLocalDate()
                .atStartOfDay();
        
        // 转换回Date
        return Date.from(result.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 方法3：简化版本，直接使用LocalDate
     * @param date 原始日期
     * @return 减1天后的0点0分0秒日期
     */
    public static Date subtractOneDaySimple(Date date) {
        if (date == null) {
            return null;
        }
        
        // 转换为LocalDate，减1天，然后转回Date（自动设置为0点0分0秒）
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .minusDays(1);
        
        return Date.from(localDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // 创建测试日期
        Date originalDate = new Date(); // 当前日期时间
        System.out.println("原始日期: " + sdf.format(originalDate));
        System.out.println();
        
        // 测试方法1：使用Calendar
        Date result1 = subtractOneDayWithCalendar(originalDate);
        System.out.println("方法1 - Calendar实现:");
        System.out.println("结果: " + sdf.format(result1));
        System.out.println();
        
        // 测试方法2：使用LocalDateTime
        Date result2 = subtractOneDayWithLocalDateTime(originalDate);
        System.out.println("方法2 - LocalDateTime实现:");
        System.out.println("结果: " + sdf.format(result2));
        System.out.println();
        
        // 测试方法3：简化版本
        Date result3 = subtractOneDaySimple(originalDate);
        System.out.println("方法3 - 简化版本:");
        System.out.println("结果: " + sdf.format(result3));
        System.out.println();
        
        // 测试边界情况：月初
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 1, 15, 30, 45); // 2024年1月1日 15:30:45
        Date monthStart = cal.getTime();
        System.out.println("边界测试 - 月初日期: " + sdf.format(monthStart));
        System.out.println("减1天后: " + sdf.format(subtractOneDayWithCalendar(monthStart)));
        System.out.println();
        
        // 测试null值
        System.out.println("Null测试: " + subtractOneDayWithCalendar(null));
    }
}