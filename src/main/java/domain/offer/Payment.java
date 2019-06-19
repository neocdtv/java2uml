package domain.offer;

/**
 * @author xix
 */
public enum Payment {
	CASH, LEASING;

	private String externalHandler;
	private InterSource interSource;
	private ExternalSource externalSource;
}
