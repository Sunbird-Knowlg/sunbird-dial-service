package dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Model Class for DIAL Code
 * 
 * @author gauraw
 *
 */
public class QRCodesBatch implements Serializable {

	private static final long serialVersionUID = 6038730711148121947L;

	private UUID processid;
	private String channel;
	private Map<String,String> config;
	private String createdOn;
	private List<String> dialcodes;
	private String publisher;
	private int status;
	private String url;


	public QRCodesBatch() {

	}

	public UUID getProcessid() {
		return processid;
	}

	public void setProcessid(UUID processid) {
		this.processid = processid;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public List<String> getDialcodes() {
		return dialcodes;
	}

	public void setDialcodes(List<String> dialcodes) {
		this.dialcodes = dialcodes;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
