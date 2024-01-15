package com.zyd.lib_log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class DateUtil {

    private enum WeekEnum {
        SUNDAY("星期日"),
        MONDAY("星期一"),
        TUESDAY("星期二"),
        WEDNESDAY("星期三"),
        THURSDAY("星期四"),
        FRIDAY("星期五"),
        SATURDAY("星期六");

        String name;

        WeekEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private enum WeekEnum2 {
        SUNDAY("周日"),
        MONDAY("周一"),
        TUESDAY("周二"),
        WEDNESDAY("周三"),
        THURSDAY("周四"),
        FRIDAY("周五"),
        SATURDAY("周六");

        String name;

        WeekEnum2(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 判断两个日期相差的天数
     */
    public static int differentDays(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        /**
         * 不同一年
         */
        if (year1 != year2) {
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                //闰年
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {
                    timeDistance += 366;
                }
                //不是闰年
                else {
                    timeDistance += 365;
                }
            }
            return timeDistance + (day2 - day1);
        }
        //同一年
        else {
            return day2 - day1;
        }
    }

    /**
     * 获取预约挂号的预约时间的格式化时间
     * 如 2020-09-11 星期一 上午 10：00-10：29
     *
     * @param beginTime 开始时间 时间字符串,2020-09-11 10:00:00
     * @param endTime   结束时间 时间字符串,10:29
     */
    public static String getAppointmentFullTime(String beginTime, String endTime) {
        if (beginTime == null || beginTime.length() == 0) {
            return "";
        }
        if (endTime == null || endTime.length() == 0) {
            return "";
        }

        //年月日
        String ymd = getYMD(beginTime + ":00");

        //星期几
        String week = getWeek(beginTime, null);

        //上午或下午
        String morningOrAfternoon;
        int hourOfDay = getHourOfDay(beginTime, "yyyy-MM-dd HH:mm");
        if (hourOfDay <= 12) {
            morningOrAfternoon = "上午";
        } else {
            morningOrAfternoon = "下午";
        }

        //时分

        String hm = getHM(beginTime + ":00");

        StringBuilder result = new StringBuilder();
        if (ymd.length() != 0) {
            result.append(ymd)
                    .append("\u3000");
        }

        if (week.length() != 0) {
            result.append(week)
                    .append("\n");
        }

        result.append(morningOrAfternoon)
                .append("\u3000");

        if (hm.length() != 0) {
            result.append(hm);
        }

        return result.toString() + "-" + endTime;
    }

    /**
     * 获取指定日期的年月日
     *
     * @param time 2020-03-06 13:41:21 必须是这种格式的,否则会报错,返回""
     */
    public static String getYMD(String time) {
        if (time == null || time.length() == 0) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        long timeStamp = parseTimeToTimestamp(time, null);
        if (timeStamp == 0) {
            return "";
        }
        calendar.setTimeInMillis(timeStamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取指定日期的时分
     *
     * @param time 2020-03-06 13:41:21 必须是这种格式的,否则会报错,返回""
     */
    public static String getHM(String time) {
        if (time == null || time.length() == 0) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        long timeStamp = parseTimeToTimestamp(time, null);
        if (timeStamp == 0) {
            return "";
        }
        calendar.setTimeInMillis(timeStamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            return simpleDateFormat.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取指定日期的时,24小时制
     */
    public static int getHourOfDay(String time, String pattern) {
        if (time == null || time.length() == 0) {
            return -1;
        }
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDateFormat.parse(time));
            return calendar.get(Calendar.HOUR_OF_DAY);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取指定日期的时,12小时制
     */
    public static int getHour(String time, String pattern) {
        if (time == null || time.length() == 0) {
            return -1;
        }
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDateFormat.parse(time));
            return calendar.get(Calendar.HOUR);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取当前星期几
     * 星期一,星期二,星期三,星期四,星期五,星期六,星期日
     *
     * @param time 2018-06-01
     */
    public static String getWeek(String time, String pattern) {
        if (time == null || time.length() == 0) {
            return "";
        }
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        String Week = "";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int wek = c.get(Calendar.DAY_OF_WEEK);

        if (wek == 1) {
            Week += WeekEnum.SUNDAY.getName();
        }
        if (wek == 2) {
            Week += WeekEnum.MONDAY.getName();
        }
        if (wek == 3) {
            Week += WeekEnum.TUESDAY.getName();
        }
        if (wek == 4) {
            Week += WeekEnum.WEDNESDAY.getName();
        }
        if (wek == 5) {
            Week += WeekEnum.THURSDAY.getName();
        }
        if (wek == 6) {
            Week += WeekEnum.FRIDAY.getName();
        }
        if (wek == 7) {
            Week += WeekEnum.SATURDAY.getName();
        }
        return Week;
    }

    /**
     * 获取当前周几
     * 周一,周二,周三,周四,周五,周六,周日
     *
     * @param time 2018-06-01
     */
    public static String getWeek2(String time, String pattern) {
        if (time == null || time.length() == 0) {
            return "";
        }
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        String Week = "";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int wek = c.get(Calendar.DAY_OF_WEEK);

        if (wek == 1) {
            Week += WeekEnum2.SUNDAY.getName();
        }
        if (wek == 2) {
            Week += WeekEnum2.MONDAY.getName();
        }
        if (wek == 3) {
            Week += WeekEnum2.TUESDAY.getName();
        }
        if (wek == 4) {
            Week += WeekEnum2.WEDNESDAY.getName();
        }
        if (wek == 5) {
            Week += WeekEnum2.THURSDAY.getName();
        }
        if (wek == 6) {
            Week += WeekEnum2.FRIDAY.getName();
        }
        if (wek == 7) {
            Week += WeekEnum2.SATURDAY.getName();
        }
        return Week;
    }

    /**
     * 获取当前星期几_int类型的
     * 1-星期一
     * 2-星期二
     * 3-星期三
     * 4-星期四
     * 5-星期五
     * 6-星期六
     * 7-星期日
     * -1-异常
     *
     * @param time 2018-06-01
     */
    public static int getWeekInt(String time, String pattern) {
        if (time == null || time.length() == 0) {
            return -1;
        }
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        int wek = c.get(Calendar.DAY_OF_WEEK);
        if (wek == 1) {
            return 7;
        }
        if (wek == 2) {
            return 1;
        }
        if (wek == 3) {
            return 2;
        }
        if (wek == 4) {
            return 3;
        }
        if (wek == 5) {
            return 4;
        }
        if (wek == 6) {
            return 5;
        }
        if (wek == 7) {
            return 6;
        }
        return -1;
    }

    public static int getDay_int(String time, String pattern) {
        if (time == null || time.length() == 0) {
            return -1;
        }
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当前的年份,int类型的
     *
     * @return 1995, 1996
     */
    public static int getCurrentYear_int() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 获取当前的年份,String类型的
     *
     * @return 1995, 1996
     */
    public static String getCurrentYear_String() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.YEAR) + "";
    }

    /**
     * 获取当前的月份
     *
     * @return 1, 2, 10, 11
     */
    public static int getCurrentMonth_int() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取当前的月份,String类型的,并且保持两位
     *
     * @return 01, 02, 10, 11
     */
    public static String getCurrentMonth_string() {
        return getCurrentDate("yyyy-MM").substring(5, 7);
    }

    /**
     * 获取当前的日份
     *
     * @return 1, 2, 10, 11
     */
    public static int getCurrentDay_int() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当前的日份,String类型的,并且保持两位
     *
     * @return 01, 02, 10, 11
     */
    public static String getCurrentDay_string() {
        return getCurrentDate("yyyy-MM-dd").substring(8, 10);
    }

    /**
     * 将秒转换为时分秒,如果3600秒会转换为01小时00分00秒
     */
    public static String parseSecondsToHMS(long time) {
        time = time / 1000;
        int hour = (int) (time / (60 * 60));
        final StringBuilder hourString = new StringBuilder(hour + "小时");
        if (hour < 10) {
            hourString.delete(0, hourString.length());
            hourString.append("0").append(hour).append("小时");
        }
        System.out.println(time);
        int minute = (int) (time / 60 % 60);
        final StringBuilder minuteString = new StringBuilder(minute + "分");
        if (minute < 10) {
            minuteString.delete(0, minuteString.length());
            minuteString.append("0").append(minute).append("分");
        }
        int second = (int) (time % 60);
        final StringBuilder secondString = new StringBuilder(second + "秒");
        if (second < 10) {
            secondString.delete(0, secondString.length());
            secondString.append("0").append(second).append("秒");
        }
        return hourString.toString() + minuteString.toString() + secondString.toString();
    }

    /**
     * 获取指定时间戳多少秒之后的时间戳
     *
     * @param time    指定的时间戳 13位
     * @param seconds 多少秒之后
     * @return 多少秒之后的时间戳 13位
     */
    public static long getTimePlus(long time, int seconds) {
        if (time <= 0 || seconds <= 0) {
            return 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTimeInMillis();
    }

    /**
     * 将时间字符串转换为时间戳,13位的
     */
    public static long parseTimeToTimestamp(String time, String pattern) {
        if (time == null || time.length() == 0) {
            return 0;
        }
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDateFormat.parse(time));
            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将时间戳转换为时间,13位的
     */
    public static String parseTimestampToTime(long timestamp, String pattern) {
        if (timestamp <= 0) {
            return null;
        }
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * 根据Date获取格式化后的时间
     */
    public static String getFormatTimeByDate(Date date, String pattern) {
        if (date == null) {
            date = new Date();
        }
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 获取当前日期
     */
    public static String getCurrentDate(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }

    /**
     * 获取当前日期的前一个星期
     */
    public static String getCurrentDate_BeforeOneWeek(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.WEEK_OF_MONTH, -1);
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * 获取当前日期的前一个月
     */
    public static String getCurrentDate_BeforeOneMonth(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * 获取当前日期的前三个月
     */
    public static String getCurrentDate_BeforeThreeMonths(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -3);
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * 获取当前日期的前半年
     */
    public static String getCurrentDate_BeforeOneHalfYear(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -6);
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * 获取当前日期的前一年
     */
    public static String getCurrentDate_BeforeOneYear(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy-MM-dd";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -1);
        return simpleDateFormat.format(calendar.getTime());
    }
}
