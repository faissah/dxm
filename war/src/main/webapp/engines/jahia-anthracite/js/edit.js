(function(){
	// CONTANTS
	var PERSISTANT = true;

	var indigoQF = {
		counter: 0,
		triggerMouseEvent: function(node, eventType) {
			if(node){
				var clickEvent = document.createEvent("MouseEvents");
				clickEvent.initEvent(eventType, true, true);
				node.dispatchEvent(clickEvent);
			}

		},
		init: function(){
			// Set up INDIGO listeners (listening to changes in DOM)
			indigoQF.GWT.onTreeChange("JahiaGxtPagesTab", function(tree){
				// console.log("PAGE TREE UPDATED ...", tree);

			}, PERSISTANT);

			indigoQF.GWT.onOpen("#JahiaGxtContentPickerWindow", function(){
				// console.log("OPEN PICKER");
				indigoQF.listeners.picker("open");
			}, PERSISTANT);

			indigoQF.GWT.onOpen("#JahiaGxtEnginePanel", function(){
				// console.log("OPEN EDIT ENGINE");
				indigoQF.listeners.editEngine("open");
			}, PERSISTANT);

			indigoQF.GWT.onOpen("#JahiaGxtImagePopup", function(){
				// console.log("OPEN IMAGE POPUP");
				indigoQF.listeners.imagePreview("open");
			}, PERSISTANT);

			indigoQF.GWT.onOpen(".menu-edit-menu-mode", function(){
				// console.log("OPEN MENU EDIT MENU MODE");
			}, PERSISTANT);


			indigoQF.GWT.onOpen(".x-dd-drag-proxy", function(){
				// console.log("::: XXX ::: STARTED TO DRAG");
				indigoQF.listeners.closeSidePanel();
			}, PERSISTANT);

			indigoQF.GWT.onClose(".x-dd-drag-proxy", function(){
				// console.log("::: XXX ::: STOPPED DRAGGING");
			}, PERSISTANT);

			indigoQF.GWT.onAttr("body", "data-selection-count", function(value){
				// console.log("::: XXX ::: UPDATED MULTI SELECT");
				indigoQF.listeners.countChanged(parseInt(value));
			}, PERSISTANT);

			indigoQF.GWT.onAttr("body", "data-main-node-displayname", function(value){
				// console.log("::: XXX ::: UPDATED PAGE NAME");

				indigoQF.status.currentPage.displayname = value;
				indigoQF.listeners.displaynameChanged();

			}, PERSISTANT);

			indigoQF.GWT.onAttr("body > div:nth-child(1) > div:nth-child(1)", "class", function(value){
				// console.log("::: XXX ::: SITE HOLDER HAS CHANGED", value);
				indigoQF.listeners.changedMode(value);

			}, PERSISTANT);

			indigoQF.GWT.onAttr("body", "data-sitesettings", function(value){
				// console.log("::: XXX ::: data-sitesettings HAS CHANGED", value);

				if($("body").attr("data-sitesettings") == "true" && $("body").attr("data-edit-window-style") != "settings"){
					indigoQF.listeners.clickSidePanelSettingsTab(true);
				}

			}, PERSISTANT);

			indigoQF.GWT.onClose("#JahiaGxtContentPickerWindow", function(){
				// console.log("CLOSE PICKER");
				indigoQF.listeners.picker("close");
			}, PERSISTANT);

			indigoQF.GWT.onClose("#JahiaGxtEnginePanel", function(){
				// console.log("CLOSE EDIT ENGINE");
				indigoQF.listeners.editEngine("close");
			}, PERSISTANT);

			indigoQF.GWT.onClose("#JahiaGxtImagePopup", function(){
				// console.log("CLOSE IMAGE POPUP");
				indigoQF.listeners.imagePreview("close");
			}, PERSISTANT);



			// Copy Anthracite CSS to remove / add when dropping in and out of STUDIO mode
			indigoQF.status.css.storedCSS = $('link[rel=stylesheet][href$="edit_en.css"]').clone();

			// Attach window listeners
			window.onresize = indigoQF.listeners.windowResize;

			// Setup observers
			indigoQF.observers.body();

			// Setup listeners
			$(document).ready(function(){
				$(window).on("blur", indigoQF.listeners.windowBlur);

				// indigoQF.listeners.queue("pagesPanel", function(){
				// 	console.log("YIPEEE LOADED PAGES PANEL");
				// });

				$("body")
					.on("click", ".app-container", function(e){

						var inSidePanel = $(e.target).closest("#JahiaGxtSidePanelTabs, .edit-menu-sites, .window-side-panel #JahiaGxtRefreshSidePanelButton");
						if(inSidePanel.length == 0){
							indigoQF.listeners.closeSidePanel();
						}

					})
					.on("click", ".toolbar-item-filepreview", indigoQF.listeners.mouseClickFilePreviewButton)
					.on("mouseenter", ".toolbar-item-filepreview", indigoQF.listeners.mouseOverFilePreviewButton)
					.on("mouseleave", ".toolbar-item-filepreview", indigoQF.listeners.mouseOutFilePreviewButton)
					.on("mouseenter", ".thumb-wrap", indigoQF.listeners.mouseOverImagePickerThumb)
					.on("mouseenter", "#JahiaGxtManagerLeftTree + div .x-grid3 .x-grid3-row", indigoQF.listeners.mouseOverImagePickerRow)
					.on("click", "#JahiaGxtManagerLeftTree + div .x-grid3 .x-grid3-row", indigoQF.listeners.selectPickerFile)
					.on("click", ".x-grid3-row .x-grid3-td-size", indigoQF.listeners.clickMoreOptionsButton) // File Picker > Search > Results List
					.on("click", ".x-grid3-row .x-tree3-el", function(e){
						// Side Panel > Trees

						indigoQF.listeners.clickMoreOptionsButton(e, "x-tree3-el");
					})
					.on("click", "#JahiaGxtManagerLeftTree + div .thumb-wrap .thumb", indigoQF.listeners.clickMoreOptionsButton) // File Picker > Thumb View
					.on("click", "#JahiaGxtManagerLeftTree + div .thumb-wrap", indigoQF.listeners.selectPickerFile)
					.on("click", ".x-viewport-editmode .x-toolbar-first > table", indigoQF.listeners.toggleThemeMode)
					.on("click", ".editmode-managers-menu", indigoQF.listeners.openManagerMenu)
					.on("click", ".menu-editmode-managers-menu", indigoQF.listeners.closeManagerMenu)
					.on("mousedown", ".menu-edit-menu-mode, .menu-edit-menu-user", indigoQF.listeners.closeManagerMenu)
					.on("click", "#JahiaGxtSidePanelTabs > div:nth-child(1) > div:nth-child(2)", indigoQF.listeners.toggleSidePanelDocking)
					.on("mouseover", ".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(7)", indigoQF.listeners.mouseOverHamburger)
					.on("click", "#JahiaGxtSidePanelTabs .x-grid3-td-displayName", function(e){
						indigoQF.listeners.clickMoreOptionsButton(e, "x-grid3-td-displayName");
					})
					.on("click", "#JahiaGxtContentPickerWindow", indigoQF.listeners.closeSourcePicker)
					.on("click", "#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview", indigoQF.listeners.listView)
					.on("click", "#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-thumbsview", indigoQF.listeners.thumbView)
					.on("click", "#JahiaGxtFileImagesBrowseTab .thumb-wrap > div:nth-child(1) > div:nth-child(2) div:nth-child(1) b", indigoQF.listeners.clickMoreOptionsButton) // NOT IN USE
					.on("click", ".x-current-page-path", indigoQF.listeners.clearMultiSelection)
					.on("click", "#JahiaGxtSidePanelTabs .x-grid3-row", indigoQF.listeners.addPageToHistory)
					.on("mousedown", "#JahiaGxtManagerLeftTree .x-tab-strip-wrap li:nth-child(1)", indigoQF.listeners.closeSearchPanel)
					.on("mousedown", "#JahiaGxtManagerLeftTree .x-tab-strip-wrap li:nth-child(2)", indigoQF.listeners.openSearchPanel)
					.on("click", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header", indigoQF.listeners.changePickerSource)
					.on("click", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", indigoQF.listeners.togglePickerSourceCombo)
					.on("mouseenter", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", indigoQF.listeners.mouseOverPickerSourceTrigger)
					.on("mouseleave", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", indigoQF.listeners.mouseOutPickerSourceTrigger)
					.on("mouseenter", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div .x-grid3-row", function(e){
						// Position the preview button next to the file whilst hovering
						indigoQF.listeners.repositionFilePreviewButton(e, {
							left: -58,
							top: 0
						});

					})
					.on("mouseenter", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div .thumb-wrap", function(e){
						// Position the preview button next to the file whilst hovering
						indigoQF.listeners.repositionFilePreviewButton(e, {
							left: -52,
							top: 0
						});


					});

				// Setup side panel listeners accordingly to naviagtion style (rollover or click) as defined in indigoQF.status.panelMenu.style
				switch(indigoQF.status.panelMenu.style){
					case "click":
						$("body").on("mousedown", "#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab", indigoQF.listeners.clickSidePanelTab);
						$("body").on("mousedown", "#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", indigoQF.listeners.clickSidePanelSettingsTab);
						break;

					case "rollover":
						$("body").on("mouseenter", "#JahiaGxtSidePanelTabs", indigoQF.listeners.mouseOverSidePanelTab);
						$("body").on("mouseleave", "#JahiaGxtSidePanelTabs", indigoQF.listeners.mouseLeaveSidePanelTabs);
						$("body").on("mouseenter", ".x-panel-body.x-border-layout-ct > div:nth-child(1) > div:nth-child(1) table > tbody > tr > td > div > table > tbody > tr > td:nth-child(1) input[type='text']", indigoQF.listeners.mouseEnterSiteSelector)
						$("body").on("mouseover", "#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab, #JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", indigoQF.listeners.mouseEnterSidePanelTab);
						break;
				}

			});

		},
		status: {
			css: {
				storedCSS: null,
				active: true
			},
			sidePanelTabs: {
				mouseOutTimer: null,
				justBeenClosed: false,
				firstLoad: true
			},
			panelMenu: {
				style: "click",
				openedJoint: null,
				mouseOutTimer: null,
				mouseOutTimeValue: 200,
				allowClickToCloseSubMenu: false,
				autoHideOnMouseOut: true,
				observer: null
			},
			publication: null,
			multiselection: {
				count: 0
			},
			currentPage: {
				displayname: null
			},
			iframeLoaded: null,
			iframeObserver: null,
			user: {},
			filePicker: {
				currentItem: null
			}
		},
		config: {
			selectors: { // Ask Thomas for classes where possible
				editModePageName: ".x-border-panel.x-border-layout-ct > div:nth-child(1) > div:nth-child(1) > div:nth-child(1)",
				editModeMoreInfo: "body[data-selection-count='0'] .x-panel-body.x-border-layout-ct > div:nth-child(2) .x-panel-header > div:nth-child(2) > table > tbody > tr > td > div > table > tbody > tr > td:nth-child(5)",
				contributeModeLanguageSelector: ".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div input",
				editModeLanguageSelector: ".mainmodule-head-container .toolbar-itemsgroup-languageswitcher",
				closeSidePanelCapture: "[data-INDIGO-GWT-SIDE-PANEL='open'] .x-panel-body.x-border-layout-ct > div:nth-child(1) > div:nth-child(2) > div:nth-child(2)",
				closeSidePanelCapture: "[data-INDIGO-GWT-SIDE-PANEL='open'] .gwt-body-edit"
			}
		},
		GWT: {
			queues: {
				update: {
					"settingsPanel": [],
					"pagesPanels": []
				}
			},
			trigger: function(params){
				var eventType = params.eventType,
					queueID = params.queueID,
					nodes = params.nodes,
					value = params.value,
					attribute = params.attribute,
					persistantFunctions = []; // Any persistant callbacks are stored here and readded to the queue after queue execution

				// Loop through queue ...
				if(indigoQF.GWT.queues[eventType] && indigoQF.GWT.queues[eventType][queueID]){

					// Attributes
					if(attribute){
						if(indigoQF.GWT.queues[eventType][queueID][attribute]){
							while (indigoQF.GWT.queues[eventType][queueID][attribute].length > 0) {
								queueItem = indigoQF.GWT.queues[eventType][queueID][attribute].pop();
								queueItem.callback(value);

								// If the persistant flag is true, save the callback to be reinserted to the queue after exection of while loop.
								if(queueItem.persistant){
									persistantFunctions.push(queueItem);
								}
							}

							// Add any permanenet callbacks back into the queue
							indigoQF.GWT.queues[eventType][queueID][attribute] = indigoQF.GWT.queues[eventType][queueID][attribute].concat(persistantFunctions);
						}


					} else {
						while (indigoQF.GWT.queues[eventType][queueID].length > 0) {
							queueItem = indigoQF.GWT.queues[eventType][queueID].pop();
							queueItem.callback(nodes);

							// If the persistant flag is true, save the callback to be reinserted to the queue after exection of while loop.
							if(queueItem.persistant){
								persistantFunctions.push(queueItem);
							}
						}

						// Add any permanenet callbacks back into the queue
						indigoQF.GWT.queues[eventType][queueID] = indigoQF.GWT.queues[eventType][queueID].concat(persistantFunctions);
					}



				}

			},
			on: function(params){

				var eventType = params.eventType,
					queueID = params.queueID,
					callback = params.callback,
					persistant = params.persistant,
					attribute = params.attribute;

				if(!indigoQF.GWT.queues[eventType]){
					// Not been initialised, so create entry in queue for this queueID
					indigoQF.GWT.queues[eventType] = {}
				}

				if(!indigoQF.GWT.queues[eventType][queueID]){
					// Not been initialised, so create entry in queue for this queueID
					indigoQF.GWT.queues[eventType][queueID] = []
				}

				if(attribute){
					if(!indigoQF.GWT.queues[eventType][queueID][attribute]){
						indigoQF.GWT.queues[eventType][queueID][attribute] = [];
					}

					indigoQF.GWT.queues[eventType][queueID][attribute].push({
						persistant: persistant,
						callback: callback
					});
				} else {
					indigoQF.GWT.queues[eventType][queueID].push({
						persistant: persistant,
						callback: callback
					});
				}


			},

			// SHORT HAND EVENT LISTENERS
			onUpdate: function(queueID, callback, persistant){
				indigoQF.GWT.on({
					eventType: "update",
					queueID: queueID,
					callback: callback,
					persistant: persistant
				})

			},
			onTreeChange: function(queueID, callback, persistant){
				indigoQF.GWT.on({
					eventType: "trees",
					queueID: queueID,
					callback: callback,
					persistant: persistant
				})

			},
			onOpen: function(queueID, callback, persistant){
				indigoQF.GWT.on({
					eventType: "open",
					queueID: queueID,
					callback: callback,
					persistant: persistant
				})

			},
			onClose: function(queueID, callback, persistant){
				indigoQF.GWT.on({
					eventType: "close",
					queueID: queueID,
					callback: callback,
					persistant: persistant
				})

			},
			onAttr: function(queueID, attribute, callback, persistant){
				indigoQF.GWT.on({
					eventType: "attribute",
					attribute: attribute,
					queueID: queueID,
					callback: callback,
					persistant: persistant
				})

			}

		},
		listeners: {
			// Add to queue

			// Window listeners
			windowLoad: function(){},
			windowResize: function(){
				indigoQF.listeners.updatePageMenuPositions();

			},
			windowBlur: function(){
				// Window has lost focus, so presume that the user has clicked in the iframe.
				// If the side panel is open, then close it

				if($("body").attr("data-INDIGO-GWT-SIDE-PANEL") == "open"){
					indigoQF.listeners.closeSidePanel();


				}

			},

			closeSourcePicker: function(){
				$("body").attr("data-INDIGO-PICKER-SOURCE-PANEL", "");

			},

			listView: function(){
				$("body").attr("indigo-PICKER-DISPLAY", "list");
			},
			thumbView: function(){
				$("body").attr("indigo-PICKER-DISPLAY", "thumbs");
			},

			closeSearchPanel: function(){
				// CLOSE SEARCH PANEL

				// Hide the search panel
				$("body").attr("data-INDIGO-PICKER-SEARCH", "");

				// Display the BROWSE panels
				$("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)").removeClass("x-hide-display");

				// Get the refresh button
				var refreshButton = $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel").not(".x-panel-collapsed").find(".x-tool-refresh")[0];

				// CLick on the refresh button to reload the content of the directory
				indigoQF.triggerMouseEvent(refreshButton, "click");



			},
			openSearchPanel: function(){
				// OPEN SEARCH PANEL

				// Close source picker if open
				indigoQF.listeners.closeSourcePicker();


				// Display the search panel
				$("body").attr("data-INDIGO-PICKER-SEARCH", "open");

				// Put the results in LIST mode
				$("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview").trigger("click");

				// Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
				$("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)").addClass("x-hide-display");


				// Remove the directory listing ( gives the search panel an empty start)
				setTimeout(function(){
					$("#JahiaGxtManagerTobTable .x-grid3 .x-grid3-row").remove();
				}, 250);
			},
			changePickerSource: function(){
				// CHANGE SOURCE
				// The user has changed SOURCE, so we just need to hide the combo...
				$("body").attr("data-INDIGO-PICKER-SOURCE-PANEL", "");
			},
			togglePickerSourceCombo: function(e){
				// USER HAS CLICKED THE COMBO TRIGGER
				e.stopPropagation();

				$("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");

				/// Toggle the attribute in body tag
				$("body").attr("data-INDIGO-PICKER-SOURCE-PANEL", function(id, label){
					return (label == "open") ? "" : "open";
				});
			},
			mouseOverPickerSourceTrigger: function(){
				// USER HAS ROLLED OVER THE COMBO TRIGGER
				if($("body").attr("data-indigo-picker-source-panel") != "open"){
					$("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").addClass("indigo-hover");
				}
			},
			mouseOutPickerSourceTrigger: function(){
				// USER HAS ROLLED OUT OF THE COMBO TRIGGER
				$("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");
			},
			repositionFilePreviewButton: function(e, offset){
				var offset = offset || {
						left: 0,
						top: 0
					},
					file = $(e.currentTarget),
					box = file[0].getBoundingClientRect(),
					left = box.left,
					top = box.top,
					width = box.width;

				$("#JahiaGxtManagerToolbar .toolbar-item-filepreview")
					.css({
						top: (top + (offset.top)) + "px",
						left: ((left + width) + offset.left) + "px"
					})
					.addClass("indigo-show-button");
			},
			selectPickerFile: function(){
				$(".toolbar-item-filepreview").attr("indigo-preview-button-state", "selected");
			},

			// User has changed modes
			changedMode: function(mode){
				switch(mode){
					case "x-viewport-studiomode":
						// Remove Anthracite CSS style sheet
						$('link[rel=stylesheet][href$="edit_en.css"]').remove();

						// Register the fact that it has been removed
						indigoQF.status.css.active = false;
						break;

					case "x-viewport-editmode":
					case "x-viewport-contributemode":
					case "x-viewport-adminmode":
					case "x-viewport-dashboardmode":
					default:

						if(!indigoQF.status.css.active){
							// Anthracite CSS has been removed, so plug it back in
							$("head").append(indigoQF.status.css.storedCSS);
						}

						break;
				}

			},



			// Edit Engine Controller
			imagePreview: function(state){
				switch(state){
					case "open":
						$("body").attr("data-INDIGO-IMAGE-PREVIEW", "open");

						// Attribute used to display the friendly name in edit panel
						$(".engine-panel > div.x-panel-header .x-panel-header-text").attr("data-friendly-name", "nodeDisplayName");
						break;

					case "close":
						$("body").attr("data-INDIGO-IMAGE-PREVIEW", "");
						break;
				}
			},

			editEngine: function(state){
				var nodeDisplayName = $("body").attr("data-singleselection-node-displayname");

				switch(state){
					case "open":
						$("body").attr("data-INDIGO-EDIT-ENGINE", "open");

						// Attribute used to display the friendly name in edit panel
						$(".engine-panel > div.x-panel-header .x-panel-header-text").attr("data-friendly-name", nodeDisplayName);
						break;

					case "close":
						$("body").attr("data-INDIGO-EDIT-ENGINE", "");
						break;
				}
			},

			// Picker Controller
			picker: function(state){
				switch(state){
					case "open":

						$("body")
							.attr("data-INDIGO-PICKER-SEARCH", "")
							.attr("data-INDIGO-PICKER", "open")
							.attr("indigo-PICKER-DISPLAY", "thumbs");
						break;

					case "close":
						$("body").attr("data-INDIGO-PICKER", "");
						break;
				}
			},

			// Clear Multi select
			clearMultiSelection: function(e){
				$("iframe").trigger("click");
			},

			panelMenuModifyDOM: function(){
				/* PROBLEM ::: CONTENT OF GRID ROW IS EMPTY AT THE TIME OF PROCESSING */
				// panelMenuModifyDOM() ::: Used to add class names / attributes to side panel so that it can be correctly displayed with CSS"


				var menu = $("#JahiaGxtSidePanelTabs .tab_serverSettings .x-grid3-row"),
					previousItemLevel = 0,
					relPosCounter = 0,
					parentCounter = 0;
					previousParentID = null;

				menu.each(function(index, menuItem_el){
					var menuItem = $(this),
						indentSpacer = menuItem.find(".x-tree3-el > img:nth-child(1)"), // Sub menus are 'created' by indenting menu items with a transparent GIF spacer in multiples of 18. So a width of 0 is level 1, 18 is level 2, and so on...
						indentSpacerWidth = indentSpacer.width(),
						subMenuJoint = menuItem.find(".x-tree3-el .x-tree3-node-joint"), // If the menu item has a submenu then the joint is visible (has a height).
						subMenuJointHeight = (subMenuJoint.length > 0) ? subMenuJoint.attr("style").indexOf("height") > -1 : 0,
						hasSubMenu = subMenuJointHeight > 0,
						subMenuAlreadyLoaded = $("#JahiaGxtSidePanelTabs .x-grid3-row[parent-ID='" + index + "']").length > 0,
						menuItemLevel = indentSpacerWidth / 18,
						parentItemLevel,
						parentID = null;

					if(menuItemLevel > 0){
						// Sub menu item, so get ID from previous entry as this is its parent.
						parentItemLevel = menuItemLevel - 1;
						parentID = $(menuItem_el).prevAll("[menu-item-level='" + parentItemLevel + "']").first().attr("menu-id");

						if(parentID == previousParentID){
							// Part of same sub menu
							relPosCounter++;

						} else {
							relPosCounter = 0;
						}

						previousParentID = parentID;
					} else {
						relPosCounter = null;
						parentCounter++;
					}

					menuItem
						.attr("menu-ID", index)
						.attr("menu-rel-ID", relPosCounter)
						.attr("menu-item-level", menuItemLevel)
						.attr("parent-ID", parentID)
						.attr("parent-rel-ID", parentCounter)
						.attr("has-sub-menu", hasSubMenu)
						.attr("sub-menu-available", subMenuAlreadyLoaded);


				});





				var menu = $("#JahiaGxtSidePanelTabs .tab_systemSiteSettings .x-grid3-row"),
					previousItemLevel = 0,
					relPosCounter = 0,
					parentCounter = 0;
					previousParentID = null,
					systemSiteSettingsMenuItem = true;

				menu.each(function(index, menuItem_el){
					var menuItem = $(this),
						indentSpacer = menuItem.find(".x-tree3-el > img:nth-child(1)"), // Sub menus are 'created' by indenting menu items with a transparent GIF spacer in multiples of 18. So a width of 0 is level 1, 18 is level 2, and so on...
						indentSpacerWidth = indentSpacer.width(),
						subMenuJoint = menuItem.find(".x-tree3-el .x-tree3-node-joint"), // If the menu item has a submenu then the joint is visible (has a height).
						subMenuJointHeight = (subMenuJoint.length > 0) ? subMenuJoint.attr("style").indexOf("height") > -1 : 0,
						hasSubMenu = subMenuJointHeight > 0,
						subMenuAlreadyLoaded = $("#JahiaGxtSidePanelTabs .x-grid3-row[parent-ID='" + index + "']").length > 0,
						menuItemLevel = indentSpacerWidth / 18,
						menuItemLevel = 1,
						parentItemLevel;
						relPosCounter = null;
						parentCounter++;

					menuItem
						.attr("menu-ID", index)
						.attr("menu-rel-ID", relPosCounter)
						.attr("menu-item-level", menuItemLevel)
						.attr("menu-system-site-settings", systemSiteSettingsMenuItem)
						.attr("parent-ID", "50")
						.attr("parent-rel-ID", parentCounter)
						.attr("has-sub-menu", hasSubMenu)
						.attr("sub-menu-available", subMenuAlreadyLoaded);

				});







				// Add sub menu IDs (0, 1, 2, ...)
				var previousParentID = 0,
					counter = 0;

				menu.closest("[parent-id]").each(function(index, menuItem_el){
					var parentID = $(menuItem_el).attr("parent-ID");

					if(parentID == previousParentID){
						counter++;
					} else {
						counter = 0;
					}

					$(menuItem_el).attr("sub-menu-ID", counter);

					previousParentID = parentID;
				});

			},

			// Body updates
			displaynameChanged: function(){

				var pageTitle,
					multiselect = "off";

				switch(indigoQF.status.currentPage.displayname){
					case "settings":
					case "System Site":
						// Need to trigger a click on Settings tabs to make sure that the menus are loaded in advance.
						$("#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab").trigger("click");

						// Attach an observer to the Side Panel Menu
						indigoQF.observers.panelMenuObserver();
						break;

					default:
						// Presumably in Edit Mode or Contribute Mode, in which case we need to set the page title
						switch(indigoQF.status.multiselection.count){
							case 0:
								pageTitle = indigoQF.status.currentPage.displayname;
								break;

							case 1:
								pageTitle = "1 selected item";
								pageTitle = $("body").attr("data-singleselection-node-displayname");
								multiselect = "on";


								break;

							default:
								pageTitle = indigoQF.status.multiselection.count + " selected items";
								multiselect = "on";
								break;
						}

						// Set multiselect status in body attribute...
						$("body").attr("data-multiselect", multiselect);



						// Page Title in Edit Made
						$(".x-current-page-path").attr("data-PAGE-NAME",pageTitle);

						// Page Title in Contribute Made
						$(".x-viewport-contributemode .toolbar-itemsgroup-languageswitcher").attr("data-PAGE-NAME",pageTitle);

						// Page Titles need centering
						indigoQF.listeners.updatePageMenuPositions();

						// Remove Mutation Observer used in Settings pages (if attached)
						if(indigoQF.status.panelMenu.observer){
							indigoQF.status.panelMenu.observer.disconnect();
							indigoQF.status.panelMenu.observer = null;

						}
				}


			},
			countChanged: function(count){
				// Multiple Items have been selected (in Edit Mode)
				indigoQF.status.multiselection.count = count;

				// Refresh the title of the page accordingly
				indigoQF.listeners.displaynameChanged();

				setTimeout(function(){
					$(".editModeContextMenu .x-menu-list").attr("data-selected-name", $("body").attr("data-singleselection-node-displayname"));
				}, 50);
			},
			publicationStatusChanged: function(status){
				// Publication status of the current page has changed (in edit or contribute mode). Update status accordingly.
				$("body").attr("data-PAGE-PUBLICATION-STATUS", status);

			},

			updatePageMenuPositions: function(){
				// Center title to page and move surrounding menus to right and left.
				// Ask Thomas for a body attribute to distinguish EDIT and CONTRIBUTE modes.

				// EDIT MODE page title positions
				var editMode = {};
					editMode.pageNameLeft = parseInt($(indigoQF.config.selectors.editModePageName).position().left);
					editMode.pageNameWidth = Math.floor($(indigoQF.config.selectors.editModePageName).width()) - 1;
					editMode.pageNameRight = editMode.pageNameLeft + editMode.pageNameWidth;

				// Preview Menu
				$(".edit-menu-view").css({
					"left": (editMode.pageNameRight + 76) + "px",
					"opacity": 1
				});

				// Publication Menu
				$(".edit-menu-publication").css({
					"left": (editMode.pageNameRight + 65) + "px",
					"opacity": 1
				});

				// More Info Menu (previously labeled as Edit )
				$(indigoQF.config.selectors.editModeMoreInfo).css({
					"left": (editMode.pageNameLeft + 92) + "px",
					"opacity": 1
				});

				// More Language Menu (previously labeled as Edit )
				$(indigoQF.config.selectors.editModeLanguageSelector).attr("style", "left: " + (editMode.pageNameLeft + 92) + "px !important; opacity: 1");

				// CONTRIBUTE MODE page title positions
				var contributeMode = {};
					contributeMode.pageNameWidth = function(){
						/* Because the Page Title is an ::after we can not access it via Jquery, have to get the computed width of the pseudo element ... */
						var pageNameElement = document.querySelector('.x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div'),
							returnValue = 0;

						if(pageNameElement){
							returnValue = parseInt(window.getComputedStyle(pageNameElement, '::after').getPropertyValue('width'));
						}

						return returnValue;
					}();
					contributeMode.windowWidth = parseInt($("body").width());
					contributeMode.pageNameLeft = (contributeMode.windowWidth / 2) - (contributeMode.pageNameWidth / 2);
					contributeMode.pageNameRight = (contributeMode.windowWidth / 2) + (contributeMode.pageNameWidth / 2) + 20;

				// Language Selector
				$(indigoQF.config.selectors.contributeModeLanguageSelector).css({
					"margin-left": "-" + (contributeMode.pageNameWidth / 2) + "px"
				});


				// Publication Menu
				$(".contribute-menu-publication").css({
					left: contributeMode.pageNameRight + "px"
				});

				// Preview Menu
				$(".contribute-menu-view").css({
					left: (contributeMode.pageNameRight + 10) + "px"
				});

				// Edit Button
				$(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(4) > table").css({
					left: (contributeMode.pageNameRight) + "px"
				});
			},

			// Button listeners
			toggleThemeMode: function(e){
				// Toggle the UI Theme by changing the body attribute accordingly.

				/* The button firing this event is actually a pseudo element atached to a table.
				// The tables CSS has been set to ignore all pointer events EXCEPT the pseudo element who accepts pointer events.
				// This allows us to capture a click on the pseudo element, but we have to check that it a child of the table want the one that was clicked */

				if($(e.target).hasClass("x-toolbar-ct")){
					$("body").attr("data-INDIGO-UI", function(index, attr){
						return (attr == "light") ? "dark" : "light";
					});

				}
			},
			openManagerMenu: function(){
				// Close the side panel if it is open.

			},
			closeManagerMenu: function(){
				// Manager Menu has been closed by clicking on the X.
				// Can not remove the actual DOM node as it causes problems with GWT, so just hide it instead.
				$(".menu-editmode-managers-menu").fadeOut();
			},
			toggleSidePanelDocking: function(e){
				// This listener has a dual purpose depending on where it was called from.
				// If called from the Edit Mode then it toggles the Side Panel Menu as PINNED and FLOATING
				// If it is called from the settings window then it acts as a close button, closing the settings and returning to the Edit Mode.

				var windowStyle = $("body").attr("data-edit-window-style");

				switch(windowStyle){
					case "settings":
						// SETTINGS MODE: Button acts as a button that closed the Settings Overlay Page

						$("body").attr("data-edit-window-style", "default");
						indigoQF.listeners.closeSidePanel()

						// Load the last page displayed in the Edit Mode. Technically this should never be NULL. However, need to assign a value on first window load as it is currently only assigned when a user clicks a page in the Page Tree.
						if(indigoQF.status.lastOpenedPage){
							indigoQF.status.lastOpenedPage.trigger("mousedown");

						} else {
							// Could not find a page in the history so select the first page in the tree.
							// Note that this probably will never work, because by default the tree is collapsed and item #2 is not yet loaded, so a trigger click wont work as it is not there.
							// Also, we can not click the first element in the tree because it isnt actually a clickable page.
							// To solve this problem, if there isnt a nth-child(2), then the first child first needs expanding (clicking).
							$("#JahiaGxtPagesTab .x-grid3-row:nth-child(2)").trigger("mousedown");
						}

						break;
					default:
						// EDIT MODE: Button acts as a toggle for the side panel
						$("body").attr("data-INDIGO-GWT-FULLSCREEN", function(index, attr){
							return (attr == "on") ? "" : "on";
						})
						break;
				}



			},
			clickSidePanelSettingsTab: function(forceClick){
				// User has clicke the Settings Tab Button.
				if(indigoQF.status.currentPage.displayname != "settings" && ($("body").attr("data-sitesettings") == "false" || forceClick)){
					$("body").attr("data-edit-window-style", "settings");

					indigoQF.listeners.openSidePanel()

					if(indigoQF.status.lastSettingsPage){
						// Found settings page in history so open it
						indigoQF.status.lastSettingsPage.trigger("click");
					} else {
						// Trigger click on first list item WHEN it has loaded...

						// PUT BACK HERE


						// indigoQF.GWT.onTreeChange("settings", function(){
						// 	var firstInList = $("#JahiaGxtSidePanelTabs #JahiaGxtSettingsTab .x-grid3-row:nth-child(1)")[0];
						// 	indigoQF.triggerMouseEvent(firstInList, "mousedown");
						// 	indigoQF.triggerMouseEvent(firstInList, "click");
						// });

					}
				}


			},
			clickSidePanelTab: function(){
				// User has clicked on one of the side panel tabs (except for Settings Tab which calls indigoQF.listeners.clickSidePanelSettingsTab)

				var clickedTabID = $(this).attr("id");

				$("body").attr("data-INDIGO-GWT-PANEL-TAB", clickedTabID);

				// Menus for the Tabs that call this listener require a normal side panel display
				$("body").attr("data-edit-window-style", "default");

				var tabMenuActive = $(this).hasClass("x-tab-strip-active"),
					sidePanelOpen = $("body").attr("data-INDIGO-GWT-SIDE-PANEL") == "open";

				if(tabMenuActive && sidePanelOpen){
					// CLOSE SIDE PANEL: Already open for current Tab Menu
					indigoQF.listeners.closeSidePanel()
				} else {
					// OPEN SIDE PANEL.
					indigoQF.listeners.openSidePanel()

				}



			},
			closeSidePanel: function(){

				if($("body").attr("data-edit-window-style") != "settings"){
					$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "");

					// Revert iframes body style attribute to what it was originally
					$(".window-iframe").contents().find("body").attr("style", indigoQF.status.sidePanelTabs.iframeBodyStyle);

				}


			},
			addPageToHistory: function(){
				if($("body").attr("data-sitesettings") != "true" && $("body").attr("data-main-node-displayname") != "settings"){
					var openedPage = $(this).closest("#JahiaGxtPagesTab").length > 0,
						openedSettings = $(this).closest("#JahiaGxtSettingsTab").length > 0;

						if(openedPage){
							indigoQF.status.lastOpenedPage = $(this);
						} else if(openedSettings){
							indigoQF.status.lastSettingsPage = $(this);
						}

				}


			},
			openSidePanel: function(){
				$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "open");

				// GWT has problems populating the site page tree when the side panel is hidden.
				// Solution: When the side panel is opened for the FIRST TIME ONLY, the refresh button is triggered and the sites page tree is populated correctly.
				if($("body").attr("data-sitesettings") == "false"){
					if(indigoQF.status.sidePanelTabs.firstLoad){
						$(".window-side-panel #JahiaGxtRefreshSidePanelButton").trigger("click");
						indigoQF.status.sidePanelTabs.firstLoad = false;
					}

					// SAVE the curent style properties of the iframes body tag so we can revert to it once the side panel is closed.
					indigoQF.status.sidePanelTabs.iframeBodyStyle = $(".window-iframe").contents().find("body").attr("style") || "";

					// Remove pointer events from the iframes body, which means that once a user clicks on the iframe to exit the side panel, the content is not automatically selected.
					$(".window-iframe").contents().find("body").attr("style", indigoQF.status.sidePanelTabs.iframeBodyStyle + " pointer-events: none !important");
				}


			},
			mouseEnterSidePanelTab: function(){
				// Trigger click on Side Panel Tabs on hover
				$(this).trigger("click");
			},
			mouseEnterSiteSelector: function(){
				// Mouseover Site Selector
				// Problem: The Site Selector is displayed as if it is part of the side panel. Only Problem is that it is not a child of Side Panel, so
				//			When the user hovers it the Side Panel is effectivly mouseout-ed.
				// Fix:		Reopen the side panel as soon as the Site Selector is hovered.

				indigoQF.listeners.openSidePanel()
			},
			mouseOverSidePanelTab: function(){
				// Mouseover Side Panel tabs, so open it.
				if($("body").attr("data-selection-count") == "0"){
					indigoQF.listeners.openSidePanel()

				}
			},
			mouseLeaveSidePanelTabs: function(e){
				// CHECK if the user has actually left the Side Panel OR if they have just opened a context menu, in which case keep the Side Panel Open.
				// Note that this only applies when the Side Panel is activated on mouse over.

				if($("body > div.x-menu").length > 0){
					// A Context Menu has been found, so do not close the Side Panel.

				} else {
					// No Context menu found, so assume that the Side Panel has really been mouseout-ed - close it.

					indigoQF.listeners.closeSidePanel()

					// Set flag and timer to remove after 100ms.
					indigoQF.status.sidePanelTabs.justBeenClosed = true;
					indigoQF.status.sidePanelTabs.mouseOutTimer = setTimeout(function(){
						// Mouseou-ed more than 100ms ago, so forget about it.
						indigoQF.status.sidePanelTabs.justBeenClosed = false;
					}, 100);
				}

			},
			mouseOverHamburger: function(){
				// Mouseover Hamburger
				// Problem: When the Side Panel is open and you hover the hamburger the Side Panel closes.
				// Fix: Once the user leaves the Side Panel there is a count down started (100ms). If the user hovers the Hamburger within those 100ms
				//		we reopen it, almost as if it never closed.

				if(indigoQF.status.sidePanelTabs.justBeenClosed && $("body").attr("data-selection-count") == "0"){
					// Side Panel was open less than 100ms ago, so repopen it.
					indigoQF.listeners.openSidePanel()

				}
			},
			clickMoreOptionsButton: function(e, matchClass){
				// Open Context Menu when clicking "More" button.
				// if matchClass is passed, then the click is ONLY accepted if the clicked element has that class.
				// if matchClass is not passed then it is accepted.

				var acceptClick = (matchClass) ? $(e.target).hasClass(matchClass) : true,
					eV;

				if(acceptClick){
					eV = new jQuery.Event("contextmenu");
					eV.clientX = e.pageX;
					eV.clientY = e.pageY;

					$(e.target).trigger(eV);
				}

			},
			mouseOverImagePickerRow: function(e){
				indigoQF.status.filePicker.currentItem = $(this)[0];
				indigoQF.status.filePicker.title = $(this).find(".x-grid3-col-name").html();

				if($(this).hasClass("x-grid3-row-selected")){
					$(".toolbar-item-filepreview").attr("indigo-preview-button-state", "selected");

				} else {
					$(".toolbar-item-filepreview").attr("indigo-preview-button-state", "unselected");

				}

				$(".toolbar-item-filepreview").attr("indigo-preview-button", "show");
			},
			mouseOverImagePickerThumb: function(e){
				indigoQF.status.filePicker.currentItem = $(this)[0];
				indigoQF.status.filePicker.title = $(this).attr("id");

				if($(this).hasClass("x-view-item-sel")){
					$(".toolbar-item-filepreview").attr("indigo-preview-button-state", "selected");

				} else {
					$(".toolbar-item-filepreview").attr("indigo-preview-button-state", "unselected");

				}

				$(".toolbar-item-filepreview").attr("indigo-preview-button", "show");

			},
			mouseOverFilePreviewButton: function(){
				$(indigoQF.status.filePicker.currentItem)
					.addClass("x-view-over")
					.addClass("x-grid3-row-over");
			},
			mouseOutFilePreviewButton: function(){
				$(indigoQF.status.filePicker.currentItem)
					.removeClass("x-view-over")
					.removeClass("x-grid3-row-over");
			},
			mouseClickFilePreviewButton: function(e, secondClick){
				indigoQF.triggerMouseEvent(indigoQF.status.filePicker.currentItem, "mousedown");
				indigoQF.triggerMouseEvent(indigoQF.status.filePicker.currentItem, "mouseup");

				if(!secondClick){
					$("#JahiaGxtImagePopup").remove(); // remove OLD preview
					$(this).trigger("click", [true]); // Reopen with new preview
					$("#JahiaGxtImagePopup .x-window-bwrap").attr("data-file-name", indigoQF.status.filePicker.title);

				}

				$(".toolbar-item-filepreview").attr("indigo-preview-button", "hide");

			}
		},
		observers: {
			body: function(){
				/*
					Mutation Observer attached to BODY tag.
					Listening for changes in attributes to determine if page has changed OR if the user has started a multiple selection.
				*/


				var // Configuration of the observer:
					config = {
						attributes: true,
						childList: true,
						characterData: true,
						subtree: true
					},

					// Attach Mutation Observer to the BODY tag
					target = document.body,
					// Mutation Observer
					observer = new MutationObserver(function(mutations){
						var publicationStatus,
							publishSplit,
							publishName,
							friendlyPublishName;

						// Loop through all mutations in BODY tag
						mutations.forEach(function(mutation){

							// check for onClose
							if(mutation.removedNodes.length > 0){
								for(removedID in indigoQF.GWT.queues["close"]){
									if(removedID[0] == "#"){
										// Trying tp match an element ID
										if($(mutation.removedNodes[0]).attr("id") == removedID.substring(1)){
											indigoQF.GWT.trigger({
												eventType: "close",
												queueID: removedID,
												nodes: mutation.removedNodes[0]
											});
										}
									} else if(removedID[0] == "."){
										// Pressumed to be a classname
										if($(mutation.removedNodes[0]).hasClass(removedID.substring(1))){
											indigoQF.GWT.trigger({
												eventType: "close",
												queueID: addID,
												nodes: mutation.removedNodes[0]
											});

										}
									}
								}
							}

							// check for onOpen
							if(mutation.addedNodes.length > 0){
								for(addID in indigoQF.GWT.queues["open"]){

									if(addID[0] == "#"){
										// Trying tp match an element ID
										if($(mutation.addedNodes[0]).attr("id") == addID.substring(1)){
											indigoQF.GWT.trigger({
												eventType: "open",
												queueID: addID,
												nodes: mutation.addedNodes[0]
											});
										}
									} else if(addID[0] == "."){
										// Pressumed to be a classname
										if($(mutation.addedNodes[0]).hasClass(addID.substring(1))){
											indigoQF.GWT.trigger({
												eventType: "open",
												queueID: addID,
												nodes: mutation.addedNodes[0]
											});

										}
									}


								}

								if($(mutation.addedNodes[0]).hasClass("x-grid3-row")){
									var firstBranch = $(mutation.addedNodes[0]);

									for(treeID in indigoQF.GWT.queues["trees"]){
										if(firstBranch.closest("#" + treeID).length > 0){
											indigoQF.GWT.trigger({
												eventType: "trees",
												queueID: treeID,
												nodes: mutation.addedNodes
											});
										}
									}

								}

							}

							// Check for onAttr
							if(mutation.attributeName) {
								for(selector in indigoQF.GWT.queues["attribute"]){
									if(mutation.target == $(selector)[0]){
										// Matched selector

										for(attribute in indigoQF.GWT.queues["attribute"][selector]){
											if(attribute == mutation.attributeName){
												indigoQF.GWT.trigger({
													eventType: "attribute",
													queueID: selector,
													attribute: attribute,
													value: mutation.target.attributes[attribute].value
												});

											}
										}

									}
								}
							}



							/* START MESSY */
							// Check for changes in document publication STATUS
							if($("body > div:nth-child(1)").attr("config") == "contributemode"){
								publicationStatus = $(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(7) img");

							} else {
								publicationStatus = $(".x-panel-body.x-border-layout-ct > div:nth-child(2) .x-panel-header > div:nth-child(2) > table > tbody > tr > td > div > table > tbody > tr > td:nth-child(1) img");

							}

							if(publicationStatus.attr("src") && (publicationStatus.attr("src") !== indigoQF.status.publication)){
								indigoQF.status.publication = publicationStatus.attr("src");

								publishSplit = indigoQF.status.publication.split("/");
								publishName = publishSplit[publishSplit.length - 1];
								friendlyPublishName = publishName.substr(0, publishName.length - 4);

								indigoQF.listeners.publicationStatusChanged(friendlyPublishName);

							}
							/* END MESSY */

						});
					});

				// Pass in the target node, as well as the observer options
				observer.observe(target, config);
			},
			panelMenuObserver: function(){
				//Used to listen for changes to side menu panel and update CSS accordingly
				if(!indigoQF.status.panelMenu.observer){
					// The Observer hasnt already been attached, so do it now.

					// Used to control side panel menu by CLICK
					$("body")
						.off("mouseenter", "#JahiaGxtSidePanelTabs ul.x-tab-strip li:nth-child(2)")
						.on("mouseenter", "#JahiaGxtSidePanelTabs ul.x-tab-strip li:nth-child(2)", function(){
							indigoQF.status.panelMenu.openedJoint = 50;
							$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint)
						})

					// FOR THE TRASH ?!
					$("body")
						.off("click", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row")
						.on("click", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row", function(e){

						if(indigoQF.status.panelMenu.allowClickToCloseSubMenu){
							var menuItem = $(this),
								menuID = menuItem.attr("menu-ID"),
								currentMenu = $("#JahiaGxtSidePanelTabs").attr("current-sub-menu"),
								topLevelMenuItem = menuItem.attr("menu-item-level") == 0;

							if(topLevelMenuItem){
								if(currentMenu == menuID){
									// Already open, so hide it
									indigoQF.status.panelMenu.openedJoint = null;
									$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);
								} else {
									// Open it
									indigoQF.status.panelMenu.openedJoint = menuID;
									$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);
								}

							}
						}


					});

					// Used to control left panel menu by MOUSE OVER
					$("body")
						.off("mouseenter", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row")
						.on("mouseenter", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row", function(e){
						var menuItem = $(this),
							menuID = menuItem.attr("menu-ID"),
							subMenuAvailable = menuItem.attr("sub-menu-available") == "true",
							hasSubMenu = menuItem.attr("has-sub-menu") == "true",
							currentMenu = $("#JahiaGxtSidePanelTabs").attr("current-sub-menu"),
							topLevelMenuItem = menuItem.attr("menu-item-level") == 0;

						if(hasSubMenu){
							if(currentMenu == menuID){
								// Menu already visible so hide it
								indigoQF.status.panelMenu.openedJoint = null;
								$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);

							} else {
								// Open this sub menuID
								indigoQF.status.panelMenu.openedJoint = menuID;
								$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);

								if(!subMenuAvailable){
									// Sub menu hasnt been loaded into the DOM, so trigger click the joint that will load it from GWT
									menuItem.find(".x-tree3-node-joint").trigger("click");
								}

							}
						} else {
							if(topLevelMenuItem) {
								// Mouse enter top level menu item with no sub menu, so just close any open sub menus
								indigoQF.status.panelMenu.openedJoint = null;
								$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);
							} else {
								// Mouse over sub level menu
								if(indigoQF.status.panelMenu.autoHideOnMouseOut){
									clearTimeout(indigoQF.status.panelMenu.mouseOutTimer);
								}

							}
						}
					});

					$("body")
						.off("mouseleave", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row")
						.on("mouseleave", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row", function(e){

						if(indigoQF.status.panelMenu.autoHideOnMouseOut){
							var menuItem = $(this),
								subLevelMenuItem = menuItem.attr("menu-item-level") > 0;

							if(subLevelMenuItem){

								indigoQF.status.panelMenu.mouseOutTimer = setTimeout(function(){
									indigoQF.status.panelMenu.openedJoint = null;
									$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);
									clearTimeout(indigoQF.status.panelMenu.mouseOutTimer);
								}, indigoQF.status.panelMenu.mouseOutTimeValue);


							}
						}



					});

					var config = {
							attributes: true,
							childList: true,
							characterData: true,
							subtree: true
						},
						target = document.getElementById("JahiaGxtSidePanelTabs");


						indigoQF.status.panelMenu.observer = new MutationObserver(function(mutations){
							var removedNodes;

							mutations.forEach(function(mutation){
								// Knowing when the Side Panel is loaded is a real PAIN. There is no feedback.
								// Nodes are added at a will and not even in one go, so we have to execute the callback a few times.
								// The callback is executed when the LOADING MASK has been removed from the Menu.

								removedNodes = mutation.removedNodes;

								if(removedNodes.length > 0){
								  if($(removedNodes[0]).hasClass("ext-el-mask") || $(removedNodes[0]).hasClass("x-tree3-node-joint")){
									  indigoQF.listeners.panelMenuModifyDOM();
								  }
								}


							});
						})

						// Pass in the target node, as well as the observer options
						indigoQF.status.panelMenu.observer.observe(target, config);
				} else {
					// Close Sub Menus if still open (after clicking a link)
					indigoQF.status.panelMenu.openedJoint = null;
					$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);
				}
			}
		}
	}

	// Page is ready, so start the ball rolling ...

	$(document).ready(function(){
		indigoQF.init();

	});


}());
