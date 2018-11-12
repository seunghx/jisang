package com.jisang.dto;

import java.util.List;

/**
 * 
 * 지하상가 지도 요청에 대한 응답으로 사용 될 응답 DTO 클래스이다. 기존 지하상가의 경/위도 정보를 나타내기 위해 도 단위의
 * 프로퍼티(예:topLeftLATDegree)와 분 값을 포함하는 초 단위의 프로퍼티(예:topLeftLATSec)로 구성되어 있던 이
 * 클래스의 필드는 현재 구현에서는 도 단위와 분, 초단위를 모두 포함하는 프로퍼티로 변경하였다. (예 :
 * {@code topLeftLAT, topLeftLNG})
 * 
 * 클라이언트에서 수행 할 지도 관련 화면 로직은 구현에 따라 변경될 수 있는 것인데, 특정 구현 로직의 편의(현재의 경우 제이쿼리로 구현한
 * 지상 지도 데모 로직)만을 고려하여 기존의 도 단위, 초 단위의 프로퍼티를 클라이언트에 전달하는 것은 보다 범용적이지 못하다고 생각하여
 * 데이터베이스의 저장된 경/위도 값 그대로를 클라이언트에 전달하기로 하였다.
 * 
 * 클라이언트에서는 구현 로직에 따라 전달 받은 경/위도 값으로부터 도 단위 값이 필요하면 도 단위 값을 직접 계산하고 초 단위 값이 필요하면
 * 초 단위 값을 직접 계산하면 된다.
 * 
 * 보통 1도에 경도 기준 90km, 위도 기준 110km 이기 때문에 지하상가의 위치가 경/위도 값이 변경되는 지점에 걸쳐있지 않는 한 도
 * 단위 값은 지하상가 지도를 표현하는데 있어 무의미하다.
 * 
 * 그러나 다음과 같은 두 가지 이유 때문에(앞서 말한 도의 경계에 지하상가가 겹치는 경우를 제외한 두 가지)도 단위 값을 클라이언트에
 * 전달해야 한다.
 * 
 * 
 * 첫 째는 지상 어플리케이션 사용자가 지하상가 밖의 다른 지역에서 지상 어플리케이션을 사용하는 경우이다. 우연히 분 단위, 초 단위 값은
 * 특정 지하상가의 분 단위, 초 단위 경위도 값 내에 포함되나 도 단위의 값이 다를 경우 사용자의 위치가 지하상가 지도 상에 표시되면 안되기
 * 때문이다. (동아리 앱잼 기간 동안 지상 어플리케이션 프로젝트를 진행하였을 당시 지하상가에서는 GPS가 동작하지 않는다고하여 지도 기능을
 * 구현하지 않기로 했었다. 위의 첫 번째 이유는 지하상가내에서도 GPS가 잘 동작된다는 가정이 내포되어 있다. GPS가 동작되지 않는다면
 * 현재 위치 기능은 서비스할 필요가 없기 때문이다.)
 * 
 * 다음 이유는, 후에 어플리케이션의 지도 기능이 확장되어 구글 맵 또는 네이버 지도(현재 네이버 지도의 경우 유일하게 실내지도 서비스를
 * 사용자에게 제공하고 있는 것으로 알고 있다. 그러나 실내지도 서비스는 API 수준에서는 아직 지원되지 않기 때문에 지상 어플리케이션은
 * 아쉽게도 직접 지하상가 지도를 구현하여야만 했다.)와 같은 실 외 지도 서비스와 연계하여 동작되게 할 경우를 고려하면 도 단위 값을 꼭
 * 전달하여야만 하기 때문이다.
 * 
 * 
 * 기존과 같이 도 단위와 초 단위 값을 분리하여 클라이언트에 전달할 경우 지하상가 지도 화면 로직을 수행하는 클라이언트는 본인이 화면을
 * 구성하는데 있어 직접적으로 필요한 정보인 초 단위 정보(분을 포함한 초)를 바로 전달 받을 수 있다는 장점이 있다. 그러나 이 경우 앞서
 * 말했듯 클라이언트의 화면 기능 확장 등에 따른 로직 변경 또는 추가에 취약하다.
 * 
 * 
 * @author leeseunghyun
 *
 */
public class MallMapResponseDTO {

    private double topLeftLAT;
    private double topLeftLNG;

    private double bottomRightLAT;
    private double bottomRightLNG;

    private List<MapAddressResponseDTO> addressList;

    private boolean marketMarkerIncluded;

    public static class MapAddressResponseDTO {

        private String id;

        private double topLeftLAT;
        private double topLeftLNG;
        private double bottomRightLAT;
        private double bottomRightLNG;

        private MapMarketResponseDTO market;

        private boolean marked;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getTopLeftLAT() {
            return topLeftLAT;
        }

        public void setTopLeftLAT(double topLeftLAT) {
            this.topLeftLAT = topLeftLAT;
        }

        public double getTopLeftLNG() {
            return topLeftLNG;
        }

        public void setTopLeftLNG(double topLeftLNG) {
            this.topLeftLNG = topLeftLNG;
        }

        public double getBottomRightLAT() {
            return bottomRightLAT;
        }

        public void setBottomRightLAT(double bottomRightLAT) {
            this.bottomRightLAT = bottomRightLAT;
        }

        public double getBottomRightLNG() {
            return bottomRightLNG;
        }

        public void setBottomRightLNG(double bottomRightLNG) {
            this.bottomRightLNG = bottomRightLNG;
        }

        public MapMarketResponseDTO getMarket() {
            return market;
        }

        public void setMarket(MapMarketResponseDTO market) {
            this.market = market;
        }

        public boolean isMarked() {
            return marked;
        }

        public void setMarked(boolean marked) {
            this.marked = marked;
        }

        @Override
        public String toString() {

            return getClass().getName() + "[id=" + id + ", topLeftLAT=" + topLeftLAT + ", topLeftLNG=" + topLeftLNG
                    + ", bottomRightLAT=" + bottomRightLAT + ", bottomRightLNG=" + bottomRightLNG + ", market=" + market
                    + ", marked=" + marked + "]";
        }

    }

    public static class MapMarketResponseDTO {

        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return getClass().getName() + "[id=" + id + ", name=" + name + "]";
        }
    }

    public double getTopLeftLAT() {
        return topLeftLAT;
    }

    public void setTopLeftLAT(double topLeftLAT) {
        this.topLeftLAT = topLeftLAT;
    }

    public double getTopLeftLNG() {
        return topLeftLNG;
    }

    public void setTopLeftLNG(double topLeftLNG) {
        this.topLeftLNG = topLeftLNG;
    }

    public double getBottomRightLAT() {
        return bottomRightLAT;
    }

    public void setBottomRightLAT(double bottomRightLAT) {
        this.bottomRightLAT = bottomRightLAT;
    }

    public double getBottomRightLNG() {
        return bottomRightLNG;
    }

    public void setBottomRightLNG(double bottomRightLNG) {
        this.bottomRightLNG = bottomRightLNG;
    }

    public List<MapAddressResponseDTO> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<MapAddressResponseDTO> addressList) {
        this.addressList = addressList;
    }

    public boolean isMarketMarkerIncluded() {
        return marketMarkerIncluded;
    }

    public void setMarketMarkerIncluded(boolean marketMarkerIncluded) {
        this.marketMarkerIncluded = marketMarkerIncluded;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[topLeftLAT=" + topLeftLAT + ", topLeftLNG=" + topLeftLNG + ", bottomRightLAT="
                + bottomRightLAT + ", bottomRightLNG=" + bottomRightLNG + ", addressList=" + addressList
                + ", marketMarkerIncluded=" + marketMarkerIncluded + "]";
    }

}
