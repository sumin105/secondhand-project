package study.secondhand.global.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

@Component("timeUtil")
public class TimeUtil {
    public static String formatRelative(LocalDateTime datetime) {
        if (datetime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(datetime, now);

        return formatDuration(duration);

    }

    public String formatRelative(Date date) {
        if (date == null) return "";

        LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return formatRelative(dateTime);
    }

    private static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (seconds < 60) return seconds + "초 전";
        if (minutes < 60) return minutes + "분 전";
        if (hours < 24) return hours + "시간 전";
        if (days < 7) return days + "일 전";
        if (days < 30) return (days / 7) + "주 전";
        if (days < 365) return (days / 30) + "달 전";
        return (days / 365) + "년 전";
    }

    public String formatTime(LocalDateTime time) {
        if (time == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a hh:mm")
                .withLocale(Locale.KOREAN);
        return time.format(formatter);
    }
}
