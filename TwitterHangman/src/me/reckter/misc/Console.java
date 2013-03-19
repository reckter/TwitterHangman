package me.reckter.misc;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Console
{
	public static void c_log(String modul,String titel,String text)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String uhrzeit = sdf.format(new Date());
		System.out.println("[" + uhrzeit + "][" + modul + "][" + titel + "]" + text);
		
	}
}
