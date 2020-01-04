package cn.itcast.pojo;

public class Config {

	private Integer id;
	private Integer uid;
	private String type;
	private Integer marketAmount;
	private Integer limitAmount;
	private Float maxLoss;
	private Float tradeOffset;
	private Float lossTriggerOffset;
	private Float lossEntrustOffset;
	private String lossWorkingType;
	private Integer lossType;
	private Integer rate;
	private Integer autoTrade;
	private Integer autoCancel;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getMarketAmount() {
		return marketAmount;
	}

	public void setMarketAmount(Integer marketAmount) {
		this.marketAmount = marketAmount;
	}

	public Integer getLimitAmount() {
		return limitAmount;
	}

	public void setLimitAmount(Integer limitAmount) {
		this.limitAmount = limitAmount;
	}

	public Float getMaxLoss() {
		return maxLoss;
	}

	public void setMaxLoss(Float maxLoss) {
		this.maxLoss = maxLoss;
	}

	public Float getTradeOffset() {
		return tradeOffset;
	}

	public void setTradeOffset(Float tradeOffset) {
		this.tradeOffset = tradeOffset;
	}

	public Float getLossTriggerOffset() {
		return lossTriggerOffset;
	}

	public void setLossTriggerOffset(Float lossTriggerOffset) {
		this.lossTriggerOffset = lossTriggerOffset;
	}

	public Float getLossEntrustOffset() {
		return lossEntrustOffset;
	}

	public void setLossEntrustOffset(Float lossEntrustOffset) {
		this.lossEntrustOffset = lossEntrustOffset;
	}

	public String getLossWorkingType() {
		return lossWorkingType;
	}

	public void setLossWorkingType(String lossWorkingType) {
		this.lossWorkingType = lossWorkingType;
	}

	public Integer getLossType() {
		return lossType;
	}

	public void setLossType(Integer lossType) {
		this.lossType = lossType;
	}

	public Integer getRate() {
		return rate;
	}

	public void setRate(Integer rate) {
		this.rate = rate;
	}

	public Integer getAutoTrade() {
		return autoTrade;
	}

	public void setAutoTrade(Integer autoTrade) {
		this.autoTrade = autoTrade;
	}

	public Integer getAutoCancel() {
		return autoCancel;
	}

	public void setAutoCancel(Integer autoCancel) {
		this.autoCancel = autoCancel;
	}

}
