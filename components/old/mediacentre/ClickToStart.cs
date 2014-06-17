using Microsoft.Windows.MediaCenter;
using System;

public class ClickToStart 
{

	public static void Main(String[] args) 
	{
		Console.WriteLine("Title: " + args[0]);
		Console.WriteLine("Date: " + args[1]);
		String test = ClickToStart.ProgramRecorder(args[0], args[1]);
		Console.WriteLine(test);
	}

	public static String ProgramRecorder(String title, String date) 
	{
		//Console.WriteLine("Hello");

		//string title = "test";
		//string date = "2005-10-17T18:00:00Z";

		string xml =
			"<?xml version=\"1.0\" encoding=\"utf-8\" ?> " +
			"<clickToRecord xmlns=\"urn:schemas-microsoft-com:ehome:clicktorecord\">" +
			" <body>" +
			"  <metadata><description>Scheduled with Mce Bulk Sceduler</description></metadata>" +
			"  <programRecord prepadding=\"0\" postpadding=\"0\" isRecurring=\"false\">" +
			"   <program>" +
			"    <key field=\"urn:schemas-microsoft-com:ehome:epg:program#title\" match=\"exact\">" + title + "</key>" +
			"   </program>" +
			"   <airing timeZone=\"60\" searchSpan=\"10\">" +
			"    <key field=\"urn:schemas-microsoft-com:ehome:epg:airing#starttime\">" + date + "</key>" +
			"   </airing>" +
			"  </programRecord>" +
			" </body>" +
			"</clickToRecord>";

		Console.WriteLine(xml);

		try
		{
			byte[] byteArr;
			System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();

			byteArr = encoding.GetBytes(xml);

			Microsoft.Windows.MediaCenter.ProgramDetails [] details;

			System.IO.Stream sr = new System.IO.MemoryStream(byteArr);

			Microsoft.Windows.MediaCenter.ClickToRecord.SubmitResult result = ClickToRecord.Submit(sr,
				Microsoft.Windows.MediaCenter.ClickToRecord.ConflictResolutionPolicy.AllowConflict,
				out details); 

			return result.ToString();
		} 
		catch (Exception e) 
		{
			//Console.WriteLine(e.ToString());
			return "Failed: " + e.ToString();
		}
	}
}