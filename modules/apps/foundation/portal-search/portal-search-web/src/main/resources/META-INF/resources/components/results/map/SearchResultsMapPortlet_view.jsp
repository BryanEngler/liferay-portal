<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/init.jsp" %>

<%@ page import="com.liferay.portal.search.web.components.results.map.portlet.SearchResultsMapDisplayContext" %>
<%@ page import="com.liferay.portal.search.web.components.results.list.portlet.SearchResultsListViewHelper" %>

<%@ page import="com.liferay.blogs.kernel.model.BlogsEntry" %>
<%@ page import="com.liferay.blogs.kernel.service.BlogsEntryLocalServiceUtil" %>
<%@ page import="com.liferay.message.boards.kernel.model.MBMessage" %>
<%@ page import="com.liferay.portal.kernel.search.Document" %>

<%@ page import="java.text.SimpleDateFormat" %>

<%
SearchResultsMapDisplayContext searchResultsMapDisplayContext = 
	(SearchResultsMapDisplayContext)java.util.Objects.requireNonNull(
		request.getAttribute(SearchResultsMapDisplayContext.ATTRIBUTE));
				 
SearchResultsListViewHelper dc = new SearchResultsListViewHelper(
	searchResultsMapDisplayContext.getSearchResultsData());
									 
				 
SearchContainer<Document> newSearchContainer = searchResultsMapDisplayContext.getSearchResultsContainer();
%>

<%
List<Document> searchResults = newSearchContainer.getResults();

int searchResultsAmount = searchResults.size();

String searchQuery = searchResultsMapDisplayContext.getQ();
%>

<%
PortletURL portletURL = renderResponse.createRenderURL();
%>

<liferay-frontend:management-bar
	searchContainerId="resultsContainer"
>
	<liferay-frontend:management-bar-buttons>
		<liferay-frontend:management-bar-display-buttons
			displayViews='<%= new String[] {"icon", "descriptive"} %>'
			portletURL="<%= portletURL %>"
			selectedDisplayStyle="descriptive"
		/>
	</liferay-frontend:management-bar-buttons>

	<liferay-frontend:management-bar-filters>
		<liferay-frontend:management-bar-navigation
			navigationKeys='<%= new String[] {"category", "asset-type"} %>'
			navigationParam=""
			portletURL="<%= portletURL %>"
		/>

		<liferay-frontend:management-bar-sort
			orderByCol=""
			orderByType=""
			orderColumns='<%= new String[] {"title", "display-date"} %>'
			portletURL="<%= portletURL %>"
		/>
	</liferay-frontend:management-bar-filters>
</liferay-frontend:management-bar>

<p class="search-total-label text-default">
	About <%= searchResultsAmount %> results for <strong><%= searchQuery %></strong>
</p>

<div class="open search-map-container sidenav-container" id="<portlet:namespace />searchSideNavContainer">
	<div class="sidenav-menu-slider">
		<div class="sidenav-menu">
			<%@ include file="/components/results/list/results_list.jspf" %>
		</div>
	</div>

	<div class="sidenav-content">
		<div class="map-drawing-toolbar toolbar" id="mapDrawingToolbar">
			<div class="toolbar-group">
				<div class="toolbar-group-content">
					<span class="toolbar-message"><strong>Draw an area</strong> where you would like to search.</span>
				</div>
			</div>

			<div class="toolbar-group-right">
				<div class="toolbar-group-content">
					<button class="btn btn-default" onclick="cancelOverlayMode();">Cancel</button>

					<button class="btn btn-default map-clear-button" onclick="clearOverlay();">Clear Area</button>

					<button class="btn btn-primary map-search-button" onclick="searchOverlay();">Search</button>
				</div>
			</div>
		</div>

		<div id="mapCanvas" style="height: 800px; width: 100%;"></div>
	</div>
</div>

<script>
	var circleSVG = '<svg id="SvgjsSvg1022" xmlns="http://www.w3.org/2000/svg" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" width="20" height="20"><defs id="SvgjsDefs1023"></defs><path id="SvgjsPath1024" d="M1111.35 770.888C1106.11 770.888 1101.84 775.1550000000001 1101.84 780.399C1101.84 785.644 1106.11 789.9110000000001 1111.35 789.9110000000001C1116.6 789.9110000000001 1120.86 785.644 1120.86 780.3990000000001C1120.86 775.1550000000001 1116.6 770.8880000000001 1111.35 770.8880000000001ZM1111.35 788.182C1107.06 788.182 1103.57 784.691 1103.57 780.399C1103.57 776.108 1107.06 772.617 1111.35 772.617C1115.6399999999999 772.617 1119.1299999999999 776.108 1119.1299999999999 780.399C1119.1299999999999 784.691 1115.6399999999999 788.182 1111.35 788.182Z " fill="#859cad" transform="matrix(1,0,0,1,-1101,-770)"></path></svg>';
	var polygonSVG = '<svg id="SvgjsSvg1016" xmlns="http://www.w3.org/2000/svg" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" width="22" height="16"><defs id="SvgjsDefs1017"></defs><path id="SvgjsPath1018" d="M1108.31 787.986C1108.19 787.986 1108.07 787.965 1107.96 787.923C1107.7 787.829 1107.49 787.635 1107.38 787.386L1100.71 772.483C1100.53 772.0859999999999 1100.63 771.621 1100.96 771.3309999999999C1101.29 771.0409999999999 1101.77 770.9939999999999 1102.15 771.2079999999999L1111.8200000000002 776.6809999999998L1120.0700000000002 773.5869999999998C1120.4 773.4649999999998 1120.7700000000002 773.5159999999997 1121.0500000000002 773.7239999999997C1121.3300000000002 773.9319999999997 1121.4900000000002 774.2659999999997 1121.4600000000003 774.6109999999996L1120.8500000000004 781.6389999999997C1120.8200000000004 781.9929999999997 1120.6000000000004 782.3059999999997 1120.2700000000004 782.4589999999997L1108.7500000000005 787.8869999999997C1108.6100000000004 787.9519999999998 1108.4600000000005 787.9859999999998 1108.3100000000004 787.9859999999998ZM1103.83 774.477L1108.82 785.6279999999999L1118.86 780.8969999999999L1119.28 776.0329999999999L1112.09 778.7269999999999C1111.8 778.8339999999998 1111.48 778.8079999999999 1111.22 778.6589999999999Z " fill="#859cad" transform="matrix(1,0,0,1,-1100,-771)"></path></svg>';
	var rectangleSVG = '<svg id="SvgjsSvg1019" xmlns="http://www.w3.org/2000/svg" version="1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svgjs="http://svgjs.com/svgjs" width="17" height="17"><defs id="SvgjsDefs1020"></defs><path id="SvgjsPath1021" d="M1117 771.882L1104.58 771.882C1103.6 771.882 1102.8 772.678 1102.8 773.656L1102.8 786.079C1102.8 787.058 1103.6 787.853 1104.58 787.853L1117 787.853C1117.98 787.853 1118.77 787.058 1118.77 786.079L1118.77 773.656C1118.77 772.678 1117.98 771.882 1117 771.882ZM1104.58 786.079L1104.58 773.656L1117 773.656L1117 786.079Z " fill="#859cad" transform="matrix(1,0,0,1,-1102,-771)"></path></svg>';

	var ____lat = 42.359849;
	var ____lng = -71.0586345;

	var drawingManager;
	var infoWindow;
	var map;
	var markers = [];
	var overlay;

	function DrawControl(controlContainer, map) {
		controlContainer.style.padding = '8px 10px';

		var controlWrapper = document.createElement('div');

		controlWrapper.classList.add('btn-group');
		controlWrapper.classList.add('dropdown');

		controlWrapper.innerHTML = '<button class="btn btn-default" id="mapDrawingButton" onclick="toggleDrawingMode(\'polygon\')" onmouseover="Liferay.Portal.ToolTip.show(this, \'Draw an area to search\')" type="button">' +
		polygonSVG +
		'</button>' +
		'<button class="btn btn-default dropdown-toggle" data-toggle="dropdown" type="button">' +
			'<span class="caret"></span>' +
			'<span class="sr-only">Toggle Dropdown</span>' +
		'</button>' +
		'<ul class="dropdown-menu dropdown-menu-right" role="menu">' +
			'<li><a href="javascript:;" onclick="event.preventDefault(); toggleDrawingMode(\'polygon\');">Shape</a></li>' +
			'<li><a href="javascript:;" onclick="event.preventDefault(); toggleDrawingMode(\'circle\');">Circle</a></li>' +
			'<li><a href="javascript:;" onclick="event.preventDefault(); toggleDrawingMode(\'rectangle\');">Rectangle</a></li>' +
		'</ul>';

		controlContainer.appendChild(controlWrapper);
	}

	function toggleDrawingMode(mode) {
		var drawingButtonElement = document.getElementById('mapDrawingButton');
		var drawingToolbarElement = $('#mapDrawingToolbar');

		var drawingMode = drawingManager.getDrawingMode();

		var mapCanvasElement = $('#mapCanvas');

		var mapCanvasHeight = mapCanvasElement.outerHeight();
		var toolbarHeight = drawingToolbarElement.outerHeight();

		if (drawingMode === mode) {
			drawingManager.setDrawingMode(null);

			drawingButtonElement.style.backgroundColor = '#FFF';

			drawingToolbarElement.hide();

			mapCanvasElement.height(mapCanvasHeight + toolbarHeight);
		}
		else {
			drawingManager.setDrawingMode(mode);

			drawingButtonElement.onclick = function() {
				toggleDrawingMode(mode);
			};

			drawingButtonElement.style.backgroundColor = '#EDF0F3';

			if (mode === 'circle') {
				drawingButtonElement.innerHTML = circleSVG;
			}
			else if (mode === 'polygon') {
				drawingButtonElement.innerHTML = polygonSVG;
			}
			else if (mode === 'rectangle') {
				drawingButtonElement.innerHTML = rectangleSVG;
			}

			if (!drawingToolbarElement.is(':visible')) {
				drawingToolbarElement.show();

				mapCanvasElement.height(mapCanvasHeight - toolbarHeight);
			}
		}
	}

	function cancelOverlayMode() {
		clearOverlay();

		drawingManager.setDrawingMode(null);

		var drawingToolbarElement = $('#mapDrawingToolbar');

		drawingToolbarElement.hide();

		var mapCanvasElement = $('#mapCanvas');

		var mapCanvasHeight = mapCanvasElement.outerHeight();
		var toolbarHeight = drawingToolbarElement.outerHeight();

		mapCanvasElement.height(mapCanvasHeight + toolbarHeight);

		$('#mapDrawingButton').css('background-color', '#FFF');

		toggleSearchClearButtons(true);
	}

	function clearOverlay() {
		if (overlay) {
			overlay.setMap(null);
		}

		toggleSearchClearButtons(true);
	}

	function searchOverlay() {
		if (overlay) {
			var bounds = overlay.getBounds();

			for (var i = 0; i < markers.length; i++) {
				var contained = bounds.contains(markers[i].getPosition());

				if (contained) {
					markers[i].setMap(map);
				}
				else {
					markers[i].setMap(null);
				}
			}

			toggleSearchClearButtons(false);
		}
	}

	function toggleSearchClearButtons(search) {
		var clearButton = $('#mapDrawingToolbar .map-clear-button');
		var searchButton = $('#mapDrawingToolbar .map-search-button');

		if (search && !searchButton.is(':visible')) {
			clearButton.hide();

			searchButton.show();
		}
		else if (!search && !clearButton.is(':visible')) {
			clearButton.show();

			searchButton.hide();
		}
	}

	function ZoomControl(controlContainer, map) {
		controlContainer.style.padding = '8px 10px';

		var controlWrapper = document.createElement('div');

		controlWrapper.classList.add('btn-group-vertical');

		controlContainer.appendChild(controlWrapper);

		var zoomInButton = document.createElement('button');

		zoomInButton.classList.add('btn');
		zoomInButton.classList.add('btn-default');
		zoomInButton.innerHTML = '<svg class="lexicon-icon">' +
		'<use xlink:href="<%= themeDisplay.getPathThemeImages() %>/lexicon/icons.svg#plus" /></svg>';

		controlWrapper.appendChild(zoomInButton);

		var zoomOutButton = document.createElement('button');

		zoomOutButton.classList.add('btn');
		zoomOutButton.classList.add('btn-default');
		zoomOutButton.innerHTML = '<svg class="lexicon-icon">' +
		'<use xlink:href="<%= themeDisplay.getPathThemeImages() %>/lexicon/icons.svg#hr" /></svg>';

		controlWrapper.appendChild(zoomOutButton);

		google.maps.event.addDomListener(
			zoomInButton,
			'click',
			function() {
				map.setZoom(map.getZoom() + 1);
			}
		);

		google.maps.event.addDomListener(
			zoomOutButton,
			'click',
			function() {
				map.setZoom(map.getZoom() - 1);
			}
		);
	}

	function MapTypeControl(controlContainer, map) {
		controlContainer.style.padding = '8px 10px';

		var controlWrapper = document.createElement('div');

		controlWrapper.classList.add('btn-group');

		controlContainer.appendChild(controlWrapper);

		var mapButton = document.createElement('button');

		mapButton.classList.add('btn');
		mapButton.classList.add('btn-default');
		mapButton.innerHTML = 'Map';

		controlWrapper.appendChild(mapButton);

		var satelliteButton = document.createElement('button');

		satelliteButton.classList.add('btn');
		satelliteButton.classList.add('btn-default');
		satelliteButton.innerHTML = 'Satellite';

		controlWrapper.appendChild(satelliteButton);

		if (map.getMapTypeId() === 'roadmap') {
			mapButton.classList.add('selected');
		}
		else if (map.getMapTypeId() === 'satellite') {
			satelliteButton.classList.add('selected');
		}

		google.maps.event.addDomListener(
			mapButton,
			'click',
			function() {
				map.setMapTypeId('roadmap');

				if (!mapButton.classList.contains('selected')) {
					mapButton.classList.toggle('selected');
					satelliteButton.classList.toggle('selected');
				}
			}
		);

		google.maps.event.addDomListener(
			satelliteButton,
			'click',
			function() {
				map.setMapTypeId('satellite');

				if (!satelliteButton.classList.contains('selected')) {
					mapButton.classList.toggle('selected');
					satelliteButton.classList.toggle('selected');
				}
			}
		);
	}

	function SideNavControl(controlContainer, map) {
		var controlButton = document.createElement('div');

		controlButton.classList.add('search-list-sidebar-toggle');
		controlButton.classList.add('text-default');
		controlButton.id = '<portlet:namespace />searchSidebarToggle';
		controlButton.innerHTML = '&#8811;';

		controlContainer.appendChild(controlButton);

		$(controlButton).sideNavigation(
			{
				container: '#<portlet:namespace />searchSideNavContainer',
				equalHeight: false,
				gutter: '0',
				position: 'right',
				width: '370px'
			}
		);

		google.maps.event.addDomListener(
			controlButton,
			'mouseover',
			function() {
				if ($(controlButton).sideNavigation('visible')) {
					Liferay.Portal.ToolTip.show(this, 'Collapse Side Panel');
				}
				else {
					Liferay.Portal.ToolTip.show(this, 'Expand Side Panel');
				}
			}
		);

		var searchSidebarContainer = $('#<portlet:namespace />searchSideNavContainer');

		searchSidebarContainer.on(
			'openStart.lexicon.sidenav',
			function() {
				controlButton.innerHTML = '&#8811;';
			}
		);

		searchSidebarContainer.on(
			'closedStart.lexicon.sidenav',
			function() {
				controlButton.innerHTML = '&#8810;';
			}
		);

		searchSidebarContainer.on(
			'closed.lexicon.sidenav',
			function() {
				google.maps.event.trigger(map, 'resize');
			}
		);
	}

	function initMap() {
		if (!google.maps.Polygon.prototype.getBounds) {
			google.maps.Polygon.prototype.getBounds = function() {
				var bounds = new google.maps.LatLngBounds();

				this.getPath().forEach(
					function(element,index) {
						bounds.extend(element);
					}
				);

				return bounds;
			}
		}

		infoWindow = new google.maps.InfoWindow({
			content: 'Boston City Hall'
		});

		// Map Options
		var mapOptions = {
			center: new google.maps.LatLng(____lat, ____lng),
			mapTypeControl: false,
			maxZoom: 18,
			scrollwheel: false,
			streetViewControl: false,
			zoom: 10,
			zoomControl: false,
		};

		// Markers
		var bounds = new google.maps.LatLngBounds();

		var searchLocations = <%= searchResultsMapDisplayContext.getMapMarkersJSON() %>;

		map = new google.maps.Map(document.getElementById('mapCanvas'), mapOptions);

		for (var i = 0; i < searchLocations.length; i++) {
			var p = searchLocations[i];

			var latlng = new google.maps.LatLng(p.lat, p.lng);

			var marker = addMarker(
				{
					assetTypeName: p.assetTypeName,
					date: p.date,
					position: latlng,
					summary: p.summary,
					title: p.title,
					userName: p.userName
				}
			);

			bounds.extend(marker.position);
		}

		if (searchLocations.length > 0) {
			map.fitBounds(bounds);
			map.panToBounds(bounds);
		}

		// Custom Map Type Controls
		var mapTypeControlContainer = document.createElement('div');

		new MapTypeControl(mapTypeControlContainer, map);

		mapTypeControlContainer.index = 1;
		map.controls[google.maps.ControlPosition.LEFT_TOP].push(mapTypeControlContainer);

		// Custom Drawing Controls
		var drawControlContainer = document.createElement('div');

		new DrawControl(drawControlContainer, map);

		drawControlContainer.index = 1;
		map.controls[google.maps.ControlPosition.RIGHT_TOP].push(drawControlContainer);

		// Custom Zoom Controls
		var zoomControlContainer = document.createElement('div');

		new ZoomControl(zoomControlContainer, map);

		zoomControlContainer.index = 1;
		map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(zoomControlContainer);

		// SideNav Controls
		var sideNavControlContainer = document.createElement('div');

		new SideNavControl(sideNavControlContainer, map);

		sideNavControlContainer.index = 1;
		map.controls[google.maps.ControlPosition.RIGHT_CENTER].push(sideNavControlContainer);

		// Drawing Manager
		drawingManager = new google.maps.drawing.DrawingManager(
			{
				circleOptions: {
					fillOpacity: 0.2
				},
				drawingControl: false,
				drawingControlOptions: {
					drawingModes: ['circle', 'polygon', 'rectangle']
				},
				polygonOptions: {
					fillOpacity: 0.2
				},
				rectangleOptions: {
					fillOpacity: 0.2
				}
			}
		);

		drawingManager.setMap(map);

		google.maps.event.addListener(
			drawingManager,
			'overlaycomplete',
			function(event) {
				drawingManager.setOptions(
					{
						drawingControl: false
					}
				);

				drawingManager.setDrawingMode(null);

				overlay = event.overlay;

				var searchButton = $('#mapDrawingToolbar .map-search-button');
			}
		);
	}

	function addMarker(data) {
		var marker = new google.maps.Marker(
			{
				icon: {
					url: 'data:image/svg+xml,%3Csvg%20id%3D%22SvgjsSvg1000%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20version%3D%221.1%22%20xmlns%3Axlink%3D%22http%3A%2F%2Fwww.w3.org%2F1999%2Fxlink%22%20xmlns%3Asvgjs%3D%22http%3A%2F%2Fsvgjs.com%2Fsvgjs%22%20width%3D%2246%22%20height%3D%2246%22%3E%3Cdefs%20id%3D%22SvgjsDefs1001%22%3E%3Cfilter%20id%3D%22SvgjsFilter1008%22%20width%3D%22200%25%22%20height%3D%22200%25%22%20x%3D%22-50%25%22%20y%3D%22-50%25%22%3E%3CfeGaussianBlur%20id%3D%22SvgjsFeGaussianBlur1009%22%20stdDeviation%3D%221.5%20%22%20result%3D%22SvgjsFeGaussianBlur1009Out%22%20in%3D%22SourceGraphic%22%3E%3C%2FfeGaussianBlur%3E%3C%2Ffilter%3E%3CclipPath%20id%3D%22SvgjsClipPath1014%22%3E%3Cpath%20id%3D%22SvgjsPath1013%22%20d%3D%22M724%201048C735.046%201048%20744%201056.95%20744%201068C744%201079.05%20735.046%201088%20724%201088C712.954%201088%20704%201079.05%20704%201068C704%201056.95%20712.954%201048%20724%201048Z%20%22%20fill%3D%22%23ffffff%22%3E%3C%2Fpath%3E%3C%2FclipPath%3E%3C%2Fdefs%3E%3Cpath%20id%3D%22SvgjsPath1007%22%20d%3D%22M724%201050C735.046%201050%20744%201058.95%20744%201070C744%201081.05%20735.046%201090%20724%201090C712.954%201090%20704%201081.05%20704%201070C704%201058.95%20712.954%201050%20724%201050Z%20%22%20fill%3D%22%2328353d%22%20fill-opacity%3D%220.2%22%20filter%3D%22url(%23SvgjsFilter1008)%22%20transform%3D%22matrix(1%2C0%2C0%2C1%2C-701%2C-1047)%22%3E%3C%2Fpath%3E%3Cpath%20id%3D%22SvgjsPath1010%22%20d%3D%22M724%201048C735.046%201048%20744%201056.95%20744%201068C744%201079.05%20735.046%201088%20724%201088C712.954%201088%20704%201079.05%20704%201068C704%201056.95%20712.954%201048%20724%201048Z%20%22%20fill%3D%22%2365b6f0%22%20transform%3D%22matrix(1%2C0%2C0%2C1%2C-701%2C-1047)%22%3E%3C%2Fpath%3E%3Cpath%20id%3D%22SvgjsPath1011%22%20d%3D%22M724%201048C735.046%201048%20744%201056.95%20744%201068C744%201079.05%20735.046%201088%20724%201088C712.954%201088%20704%201079.05%20704%201068C704%201056.95%20712.954%201048%20724%201048Z%20%22%20fill%3D%22%2365b6f0%22%20transform%3D%22matrix(1%2C0%2C0%2C1%2C-701%2C-1047)%22%3E%3C%2Fpath%3E%3Cpath%20id%3D%22SvgjsPath1012%22%20d%3D%22M724%201048C735.046%201048%20744%201056.95%20744%201068C744%201079.05%20735.046%201088%20724%201088C712.954%201088%20704%201079.05%20704%201068C704%201056.95%20712.954%201048%20724%201048Z%20%22%20fill-opacity%3D%220%22%20fill%3D%22%23ffffff%22%20stroke-dasharray%3D%220%22%20stroke-linejoin%3D%22round%22%20stroke-linecap%3D%22round%22%20stroke-opacity%3D%221%22%20stroke%3D%22%23ffffff%22%20stroke-miterlimit%3D%2250%22%20stroke-width%3D%226%22%20clip-path%3D%22url(%26quot%3B%23SvgjsClipPath1014%26quot%3B)%22%20transform%3D%22matrix(1%2C0%2C0%2C1%2C-701%2C-1047)%22%3E%3C%2Fpath%3E%3Cpath%20id%3D%22SvgjsPath1015%22%20d%3D%22M730.003%201060L717.9970000000001%201060C716.893%201060%20715.9960000000001%201060.89%20715.9960000000001%201062L715.9960000000001%201074C715.9960000000001%201075.11%20716.8930000000001%201076%20717.9970000000001%201076L730.003%201076C731.1070000000001%201076%20732.004%201075.11%20732.004%201074L732.004%201062C732.004%201060.89%20731.107%201060%20730.003%201060ZM717.997%201074L717.997%201062L730.0029999999999%201062L730.0029999999999%201074ZM728.002%201068L719.9979999999999%201068C719.4449999999999%201068%20718.997%201068.45%20718.997%201069C718.997%201069.55%20719.4449999999999%201070%20719.9979999999999%201070L728.002%201070C728.5559999999999%201070%20729.0029999999999%201069.55%20729.0029999999999%201069C729.0029999999999%201068.45%20728.5559999999999%201068%20728.002%201068ZM725.001%201071L719.9979999999999%201071C719.4449999999999%201071%20718.997%201071.45%20718.997%201072C718.997%201072.56%20719.4449999999999%201073%20719.9979999999999%201073L725.001%201073C725.554%201073%20726.001%201072.56%20726.001%201072C726.001%201071.45%20725.554%201071%20725.001%201071ZM722.999%201064C722.999%201064.55%20723.447%201065%20724%201065C724.553%201065%20725.001%201064.55%20725.001%201064C725.001%201063.45%20724.553%201063%20724%201063C723.447%201063%20722.999%201063.45%20722.999%201064Z%20%22%20fill%3D%22%23ffffff%22%20transform%3D%22matrix(1%2C0%2C0%2C1%2C-701%2C-1047)%22%3E%3C%2Fpath%3E%3C%2Fsvg%3E'
				},
				map: map,
				position: data.position,
				title: data.title
			}
		);

		markers.push(marker);

		createInfoWindow(marker, data);

		return marker;
	}

	function createInfoWindow(marker, data) {
		var summary = data.summary

		if (summary.length > 100) {
			summary = summary.substring(0, 100) + "...";
		}

		var contentStr = '<div class="search-results-map-window-content tabular-list-group">' +
			'<div class="list-group-item-field">' +
				'<span class="search-asset-type-sticker sticker sticker-default sticker-lg sticker-rounded sticker-static">' +
					'<svg class="lexicon-icon">' +
						'<use xlink:href="<%= themeDisplay.getPathThemeImages() %>/lexicon/icons.svg#blogs" />' +
					'</svg>' +
				'</span>' +
			'</div>' +
			'<div class="list-group-item-content">' +
				'<h4><strong>' + data.title + '</strong></h4>' +
				'<h6 class="text-default">' +
					'<strong>' + data.assetTypeName + '</strong> &#183;' +
					'<liferay-ui:message key="written-by" /> <strong>' + data.userName + '</strong> ' +
					'<liferay-ui:message key="on-date" /> ' + data.date + '</h6>' +
				'<h6 class="search-document-content text-default">' + summary + '</h6>' +
			'</div>' +
			'</div>';

		google.maps.event.addListener(
			marker,
			'click',
			function() {
				infoWindow.setContent(contentStr);
				infoWindow.open(map, marker);
			}
		);
	}
</script>

<script
	src="https://maps.googleapis.com/maps/api/js?v=3&key=AIzaSyABOXmu2BMXwxNHbhHrTcMRLnOJQYpHbWQ&libraries=drawing&callback=initMap"
	type="text/javascript"></script>