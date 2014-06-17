using System;
using Microsoft.Windows.MediaCenter;

public class CSharpJniTest {

	string clickToRecordXml1 =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?> " +
		"<clickToRecord xmlns=\"urn:schemas-microsoft-com:ehome:clicktorecord\">" +
		" <body>" +
		"  <metadata><description>Scheduled with Mce Bulk Sceduler</description></metadata>" +
		"  <programRecord prepadding=\"0\" postpadding=\"0\" isRecurring=\"false\">" +
		"   <program>" +
		"    <key field=\"urn:schemas-microsoft-com:ehome:epg:program#title\" match=\"exact\">";

	string clickToRecordXml2 = "</key>" +
		"   </program>" +
		"   <airing timeZone=\"60\" searchSpan=\"10\">" +
		"    <key field=\"urn:schemas-microsoft-com:ehome:epg:airing#starttime\">";

	string clickToRecordXml3 = "</key>" +
		"   </airing>" +
		"  </programRecord>" +
		" </body>" +
		"</clickToRecord>";

	public CSharpJniTest() {}

	public void testMethod(String title, String date) {
		Console.WriteLine("Woooha fuckin hahh!!!");
		Console.WriteLine("Title: " + title);
		Console.WriteLine("Date: " + date);

		String xml = clickToRecordXml1 + title + clickToRecordXml2 + date + clickToRecordXml3;

		Console.WriteLine(xml);

		/*
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

			//return result.ToString();
		} 
		catch (Exception e) 
		{
			Console.WriteLine(e.ToString());
			//return "Failed: " + e.ToString();
		}
		*/
	}
}