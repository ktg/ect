package equip.ect.components.atomblogger;

public class UploadException extends Exception
{
	public int errorCode;
	public String errorMessage;

	public UploadException(final int errorCode, final String errorMessage)
	{
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
}
