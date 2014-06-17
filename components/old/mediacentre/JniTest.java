class JniTest
{
	static
	{
		System.loadLibrary("JniTestImpl");
	}

	public static void main(final String args[])
	{
		final JniTest test = new JniTest();
		test.testMethod(args[0], args[1]);
		// System.out.println("In java: " + temp);

	}

	public native void testMethod(String title, String date);
}