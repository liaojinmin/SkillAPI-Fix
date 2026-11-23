/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
 
var ATTRIBS = [
    'vitality',
    'spirit',
    'intelligence',
    'dexterity',
    'strength'
];

depend('sounds');
depend('filter');
depend('input');
depend('yaml');
depend('component', function() {

	// Set up component option lists
	setupOptionList(document.getElementById('triggerOptions'), Trigger, Type.TRIGGER);
	setupOptionList(document.getElementById('targetOptions'), Target, Type.TARGET);
	setupOptionList(document.getElementById('conditionOptions'), Condition, Type.CONDITION);
	setupOptionList(document.getElementById('mechanicOptions'), Mechanic, Type.MECHANIC);

	// 选择器搜索
	bindOptionSearch('triggerSearch', ['triggerOptions']);
	bindOptionSearch('componentSearch', ['targetOptions', 'conditionOptions', 'mechanicOptions']);
});
depend('material', function() {
	depend('skill', function() {
		document.getElementById('skillList').addEventListener('change', function(e) {
			activeSkill.update();
			if (activeComponent)
			{
				activeComponent.update();
			}
			if (this.selectedIndex == this.length - 1)
			{
				newSkill();
			}
			else 
			{
				activeSkill = skills[this.selectedIndex];
				activeSkill.apply();
				showSkillPage('builder');
			}
		});
		document.getElementById('skillDetails').addEventListener('click', function(e) {
			activeSkill.createFormHTML();
			showSkillPage('skillForm');
		});
		document.getElementById('saveButton').addEventListener('click', function(e) {
			saveToFile('skills.yml', getSkillSaveData());
			toast('已下载 skills.yml');
		});
		document.getElementById('saveSkill').addEventListener('click', function(e) {
			saveToFile(activeSkill.data[0].value + '.yml', activeSkill.getSaveString());
			toast('已下载 ' + activeSkill.data[0].value + '.yml');
		});
		document.getElementById('deleteSkill').addEventListener('click', function(e) {
			var list = document.getElementById('skillList');
			var index = list.selectedIndex;
			var name = skills[index] ? skills[index].data[0].value : '';

			showConfirm('确定删除技能「' + name + '」吗？此操作不可撤销。', function() {
				skills.splice(index, 1);
				if (skills.length == 0)
				{
					newSkill();
				}
				list.remove(index);
				index = Math.min(index, skills.length - 1);
				activeSkill = skills[index];
				list.selectedIndex = index;

				activeSkill.apply();
				showSkillPage('builder');
				toast('已删除技能：' + name, 'warn');
			});
		});
	});
	
	depend('class', function() {
		document.getElementById('classList').addEventListener('change', function(e) {
			activeClass.update();
			if (this.selectedIndex == this.length - 1)
			{
				newClass();
			}
			else 
			{
				activeClass = classes[this.selectedIndex];
				activeClass.createFormHTML();
			}
		});
		document.getElementById('saveButton').addEventListener('click', function(e) {
			saveToFile('classes.yml', getClassSaveData());
			toast('已下载 classes.yml');
		});
	});
});

function getSkillSaveData() {
    activeSkill.update();
    if (activeComponent) {
        activeComponent.update();
    }
    var data = 'loaded: false\n';
    var alphabetic = skills.slice(0);
    alphabetic.sort(function(a, b) {
        var an = a.data[0].value;
        var bn = b.data[0].value;
        if (an > bn) return 1;
        if (an < bn) return -1;
        return 0;
    });
    for (var i = 0; i < alphabetic.length; i++) {
        data += alphabetic[i].getSaveString();
    }
    return data;
}

function getClassSaveData() {
    activeClass.update();
    var data = 'loaded: false\n';
    for (var i = 0; i < classes.length; i++)
    {
        data += classes[i].getSaveString();
    }
    return data;
}

function setupOptionList(div, list, type) 
{
	var x;
    var i = 0;
    var output = '';
	for (x in list)
	{
        if (i % 4 == 0)
            output += '| ';
        output += '[[' + list[x].name + '|_' + type.substr(0, 1).toUpperCase() + type.substr(1) + ' ' + list[x].name + ']] | ';
        i++;
        if (i % 4 == 0)
            output += '\n';
     
		var e = document.createElement('h5');
        if (list[x].premium)
            e.className = 'premium';
		e.innerHTML = list[x].name;
		e.component = list[x];
		e.tabIndex = 0;
		e.addEventListener('mouseenter', function(e) {
			showOptionTip(this, this.component.name, getOptionDescription(this.component));
		});
		e.addEventListener('mouseleave', hideOptionTip);
		e.addEventListener('focus', function(e) {
			showOptionTip(this, this.component.name, getOptionDescription(this.component));
		});
		e.addEventListener('blur', hideOptionTip);
		e.addEventListener('keydown', function(e) {
			if (e.key === 'Enter' || e.key === ' ') {
				e.preventDefault();
				this.click();
			}
		});
		e.addEventListener('click', function(e) {
			if (activeComponent == activeSkill && activeSkill.usingTrigger(this.component.name)) {
				showSkillPage('builder');
			} else {
				showSkillPage('skillForm');
				var component = new this.component.construct();
				component.parent = activeComponent;
				activeComponent.components.push(component);
				component.createBuilderHTML(activeComponent.html);
				component.createFormHTML();
			}
		});
		div.appendChild(e);
	}
    
    //saveToFile('wiki_' + type + '.txt', output);
}

/**
 * 组件选择列表，供搜索框统一重置
 */
var OPTION_LISTS = [ 'triggerOptions', 'targetOptions', 'conditionOptions', 'mechanicOptions' ];
var OPTION_SEARCH_INPUTS = [];

/**
 * 给选择器绑定搜索框
 *
 * @param {string} inputId  - 搜索框 id
 * @param {Array}  listIds  - 需要过滤的列表 id
 */
function bindOptionSearch(inputId, listIds) {
	var input = document.getElementById(inputId);
	if (!input) return;
	OPTION_SEARCH_INPUTS.push(input);
	input.addEventListener('input', function(e) {
		var query = this.value.trim().toLowerCase();
		for (var i = 0; i < listIds.length; i++) {
			filterOptionList(listIds[i], query);
		}
	});
}

/**
 * 按关键字过滤单个列表中的 h5 选项
 */
function filterOptionList(listId, query) {
	var list = document.getElementById(listId);
	if (!list) return;
	var items = list.getElementsByTagName('h5');
	for (var i = 0; i < items.length; i++) {
		var match = query === '' || items[i].textContent.toLowerCase().indexOf(query) >= 0;
		items[i].style.display = match ? '' : 'none';
	}
}

/**
 * 清空搜索并恢复全部选项
 */
function resetOptionSearch() {
	for (var i = 0; i < OPTION_SEARCH_INPUTS.length; i++) {
		OPTION_SEARCH_INPUTS[i].value = '';
	}
	for (var j = 0; j < OPTION_LISTS.length; j++) {
		filterOptionList(OPTION_LISTS[j], '');
	}
}

/**
 * 右侧属性面板拖拽调宽
 *
 * 宽度会保存在 localStorage.formWidth，下次打开自动恢复；
 * 同时保证组件树至少保留 Math.min(560, 视口宽 - 620) 的空间。
 */
var FORM_MIN_WIDTH = 300;

function clampFormWidth(width) {
	var max = Math.max(FORM_MIN_WIDTH, Math.min(window.innerWidth * 0.6, window.innerWidth - 620));
	return Math.round(Math.max(FORM_MIN_WIDTH, Math.min(width, max)));
}

function initFormResizer() {
	var resizer = document.getElementById('formResizer');
	var form = document.getElementById('skillForm');
	if (!resizer || !form) return;

	var saved = parseInt(localStorage.getItem('formWidth'), 10);
	if (saved > 0 && window.innerWidth > 1080) {
		form.style.width = clampFormWidth(saved) + 'px';
	}

	// 与属性面板的可见状态保持一致（面板隐藏时不显示拖拽条）
	if (window.getComputedStyle(form).display !== 'none') {
		resizer.style.display = 'block';
	}

	var dragging = false;
	var rightEdge = 0;

	resizer.addEventListener('mousedown', function(e) {
		if (e.button !== 0) return;
		e.preventDefault();
		dragging = true;
		rightEdge = form.getBoundingClientRect().right;
		resizer.className = 'formResizer active';
		document.body.style.cursor = 'col-resize';
		document.body.style.userSelect = 'none';
	});

	window.addEventListener('mousemove', function(e) {
		if (!dragging) return;
		form.style.width = clampFormWidth(rightEdge - e.clientX) + 'px';
	});

	window.addEventListener('mouseup', endFormDrag);
	window.addEventListener('blur', endFormDrag);

	window.addEventListener('resize', function() {
		var current = parseInt(form.style.width, 10);
		if (current > 0) {
			form.style.width = clampFormWidth(current) + 'px';
		}
	});

	function endFormDrag() {
		if (!dragging) return;
		dragging = false;
		resizer.className = 'formResizer';
		document.body.style.cursor = '';
		document.body.style.userSelect = '';
		var width = parseInt(form.style.width, 10);
		if (width > 0) {
			localStorage.setItem('formWidth', width);
		}
	}
}

/* --------------------------------------------------------------------------
   组件介绍浮层
   -------------------------------------------------------------------------- */

var optionTipElement = null;

function ensureOptionTip() {
	if (!optionTipElement) {
		optionTipElement = document.createElement('div');
		optionTipElement.className = 'optionTip';
		document.body.appendChild(optionTipElement);
	}
	return optionTipElement;
}

/**
 * 取组件介绍：构造一次实例后缓存到定义对象上，避免每次悬停都重复构造
 */
function getOptionDescription(def) {
	if (!def) return '';
	if (typeof def.__description === 'string') return def.__description;

	var description = '';
	try {
		var instance = new def.construct();
		description = instance.description || '';
	} catch (err) {
		description = '';
	}
	def.__description = description;
	return description;
}

/**
 * 在选项下方显示介绍，空间不足时自动翻到上方并做视口内收
 */
function showOptionTip(element, name, description) {
	var tip = ensureOptionTip();

	if (!description) {
		tip.classList.remove('show');
		return;
	}

	tip.innerHTML = '';

	var title = document.createElement('div');
	title.className = 'optionTipTitle';
	title.textContent = name;
	var body = document.createElement('div');
	body.className = 'optionTipBody';
	body.textContent = description;
	tip.appendChild(title);
	tip.appendChild(body);

	tip.style.left = '0px';
	tip.style.top = '0px';
	tip.classList.add('show');

	var rect = element.getBoundingClientRect();
	var size = tip.getBoundingClientRect();

	var left = rect.left;
	if (left + size.width > window.innerWidth - 10) {
		left = window.innerWidth - size.width - 10;
	}
	if (left < 10) left = 10;

	var top = rect.bottom + 8;
	if (top + size.height > window.innerHeight - 10) {
		top = rect.top - size.height - 8;
	}
	if (top < 10) top = 10;

	tip.style.left = left + 'px';
	tip.style.top = top + 'px';
}

function hideOptionTip() {
	if (optionTipElement) {
		optionTipElement.classList.remove('show');
	}
}

// 选择器滚动时立即隐藏，避免浮层与选项错位
document.addEventListener('scroll', hideOptionTip, true);

/* --------------------------------------------------------------------------
   轻提示 / 确认弹层
   -------------------------------------------------------------------------- */

var lastToast = { message: '', time: 0 };

function toast(message, type) {
	var host = document.getElementById('toastHost');
	if (!host) return;

	// 同一时间重复触发时合并，避免弹出一串相同提示
	var now = Date.now();
	if (message === lastToast.message && now - lastToast.time < 300) return;
	lastToast = { message: message, time: now };

	var item = document.createElement('div');
	item.className = 'toast ' + (type || 'ok');
	item.textContent = message;
	host.appendChild(item);

	setTimeout(function() {
		item.className += ' out';
	}, 2200);
	setTimeout(function() {
		if (item.parentNode) item.parentNode.removeChild(item);
	}, 2800);
}

var confirmCallback = null;

function showConfirm(message, onConfirm) {
	var dialog = document.getElementById('confirmDialog');
	if (!dialog) {
		if (onConfirm) onConfirm();
		return;
	}
	document.getElementById('confirmMessage').textContent = message;
	confirmCallback = onConfirm;
	dialog.style.display = 'flex';
}

function hideConfirm() {
	var dialog = document.getElementById('confirmDialog');
	if (dialog) dialog.style.display = 'none';
	confirmCallback = null;
}

function isConfirmOpen() {
	var dialog = document.getElementById('confirmDialog');
	return !!dialog && dialog.style.display === 'flex';
}

/* --------------------------------------------------------------------------
   组件树搜索
   -------------------------------------------------------------------------- */

function filterTree(query) {
	var content = document.getElementById('builderContent');
	if (!content) return;

	var q = (query || '').trim().toLowerCase();
	var components = content.getElementsByClassName('component');
	var i;

	for (i = 0; i < components.length; i++) {
		components[i].parentNode.style.display = '';
	}
	if (q === '') return;

	for (i = 0; i < components.length; i++) {
		components[i].parentNode.style.display = 'none';
	}

	for (i = 0; i < components.length; i++) {
		var label = components[i].getElementsByTagName('h3')[0];
		if (!label || label.textContent.toLowerCase().indexOf(q) < 0) continue;

		// 命中项及其所有祖先容器保持可见
		var node = components[i].parentNode;
		while (node && node !== content) {
			if (!node.className || node.className.indexOf('componentChildren') < 0) {
				node.style.display = '';
			}
			node = node.parentNode;
		}
	}
}

function bindTreeSearch() {
	var input = document.getElementById('treeSearch');
	if (!input) return;

	input.addEventListener('input', function() {
		filterTree(this.value);
	});
	input.addEventListener('keydown', function(e) {
		if (e.key === 'Escape') {
			this.value = '';
			filterTree('');
			this.blur();
		}
	});
}

/* --------------------------------------------------------------------------
   快捷键
   -------------------------------------------------------------------------- */

function bindShortcuts() {
	document.addEventListener('keydown', function(e) {
		var key = (e.key || '').toLowerCase();
		var tag = (e.target.tagName || '').toLowerCase();
		var typing = tag === 'input' || tag === 'textarea' || tag === 'select';

		if (key === 'escape') {
			if (isConfirmOpen()) {
				hideConfirm();
				return;
			}
			if (typing) return;

			var componentChooser = document.getElementById('componentChooser');
			var triggerChooser = document.getElementById('triggerChooser');
			if ((componentChooser && componentChooser.style.display === 'block')
				|| (triggerChooser && triggerChooser.style.display === 'block')
				|| document.getElementById('skillForm').style.display === 'block') {
				showSkillPage('builder');
			}
			return;
		}

		if (!(e.ctrlKey || e.metaKey)) return;

		if (key === 's') {
			e.preventDefault();
			document.getElementById('saveButton').click();
			return;
		}

		if (key === 'f') {
			e.preventDefault();
			var chooserOpen = document.getElementById('componentChooser').style.display === 'block' ? 'componentSearch'
				: (document.getElementById('triggerChooser').style.display === 'block' ? 'triggerSearch' : 'treeSearch');
			var input = document.getElementById(chooserOpen);
			if (input) {
				input.focus();
				input.select();
			}
		}
	});
}

var skillsActive = true;

// Set up event listeners when the page loads
window.onload = function() 
{
    var isOpera = !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
    var isFirefox = typeof InstallTrigger !== 'undefined';   // Firefox 1.0+
    var isChrome = !!window.chrome && !isOpera;              // Chrome 1+
    var badBrowser = !isOpera && !isFirefox && !isChrome;
    document.getElementById('badBrowser').style.display = badBrowser ? 'block' : 'none';
    if (badBrowser) {
        return;
    }
    
	document.getElementById('addTrigger').addEventListener('click', function(e) {
		activeComponent = activeSkill;
		showSkillPage('triggerChooser');
	});
	
	document.getElementById('skillTab').addEventListener('click', function(e) {
		switchToSkills();
        
	});
	document.getElementById('classTab').addEventListener('click', function(e) {
		switchToClasses();
	});
	
	var cancelButtons = document.querySelectorAll('.cancelButton');
	for (var i = 0; i < cancelButtons.length; i++)
	{
		cancelButtons[i].addEventListener('click', function(e) {
			showSkillPage('builder');
		});
	}

	// 点击浮层空白处关闭选择器
	var choosers = document.querySelectorAll('.chooser');
	for (var i = 0; i < choosers.length; i++)
	{
		choosers[i].addEventListener('click', function(e) {
			if (e.target === this) {
				showSkillPage('builder');
			}
		});
	}

	// 右侧属性面板拖拽调宽
	initFormResizer();

	// 确认弹层
	document.getElementById('confirmCancel').addEventListener('click', hideConfirm);
	document.getElementById('confirmOk').addEventListener('click', function() {
		var callback = confirmCallback;
		hideConfirm();
		if (callback) callback();
	});
	document.getElementById('confirmDialog').addEventListener('click', function(e) {
		if (e.target === this) hideConfirm();
	});

	// 组件树搜索与快捷键
	bindTreeSearch();
	bindShortcuts();
    
    var attribs = localStorage.getItem('attribs');
    var skillData = localStorage.getItem('skillData');
    var skillIndex = localStorage.getItem('skillIndex');
    var classData = localStorage.getItem('classData');
    var classIndex = localStorage.getItem('classIndex');
    if (attribs) {
        ATTRIBS = attribs.split(",");
    }
    if (skillData) {
        skills = [];
        document.getElementById('skillList').remove(0);
        loadSkillText(skillData);
        if (skillIndex) {
            document.getElementById('skillList').selectedIndex = parseInt(skillIndex);
            activeSkill = skills[Math.max(0, Math.min(skills.length - 1, parseInt(skillIndex)))];
            activeSkill.apply();
            showSkillPage('builder');
        }
    }
    if (classData) {
        classes = [];
        document.getElementById('classList').remove(0);
        loadClassText(classData);
        if (classIndex) {
            document.getElementById('classList').selectedIndex = parseInt(classIndex);
            activeClass = classes[Math.max(0, Math.min(classes.length - 1, parseInt(classIndex)))];
            activeClass.createFormHTML();
        }
    }
    if (localStorage.getItem('skillsActive') == 'false') {
        switchToClasses();
    }
}

function switchToSkills() {
    if (!skillsActive) 
    {
        document.getElementById('skillTab').className = 'tab tabLeft tabActive';
        document.getElementById('classTab').className = 'tab tabRight';
        document.getElementById('skills').style.display = 'grid';
        document.getElementById('classes').style.display = 'none';
        skillsActive = true;
    }
}

function switchToClasses() {
    if (skillsActive) 
    {
        document.getElementById('classTab').className = 'tab tabRight tabActive';
        document.getElementById('skillTab').className = 'tab tabLeft';
        document.getElementById('classes').style.display = 'grid';
        document.getElementById('skills').style.display = 'none';
        skillsActive = false;
    }
}

/**
 * Returns the view back to the skill builder when in the skill tab
 */
function showSkillPage(name) 
{
	// 组件树常驻，属性表单作为右侧面板共存，选择器以浮层形式打开
	hideOptionTip();
	setPageStyle('builder', 'builder');
	setPageStyle('skillForm', name);
	setPageStyle('componentChooser', name);
	setPageStyle('triggerChooser', name);

	// 拖拽条只在属性面板可见时出现
	var resizer = document.getElementById('formResizer');
	if (resizer) {
		resizer.style.display = (name == 'skillForm') ? 'block' : 'none';
	}

	if (name == 'componentChooser' || name == 'triggerChooser') {
		resetOptionSearch();
	}
}

/**
 * Sets the style for the page based on the current visible one
 */
function setPageStyle(name, visible)
{
	document.getElementById(name).style.display = (visible == name ? 'block' : 'none');
}

/**
 * Represents an attribute of a skill or class
 *
 * @param {string} key   - the config key for the attribute
 * @param {double} base  - the starting value for the attribute
 * @param {double} scale - the increase of the value per level
 *
 * @constructor
 */ 
function Attribute(key, base, scale) 
{
	this.key = key;
	this.base = base;
	this.scale = scale;
}

/**
 * Saves text data to a file locally
 *
 * Code slightly modified from this page:
 * https://thiscouldbebetter.wordpress.com/2012/12/18/loading-editing-and-saving-a-text-file-in-html5-using-javascrip/
 */ 
function saveToFile(file, data) 
{
	var textFileAsBlob = new Blob([data], { type: 'text/plain;charset=utf-8' });

	var downloadLink = document.createElement("a");
	downloadLink.download = file;
	downloadLink.innerHTML = "Download File";
	if (window.webkitURL != null)
	{
		// Chrome allows the link to be clicked
		// without actually adding it to the DOM.
		downloadLink.href = window.webkitURL.createObjectURL(textFileAsBlob);
	}
	else
	{
		// Firefox requires the link to be added to the DOM
		// before it can be clicked.
		downloadLink.href = window.URL.createObjectURL(textFileAsBlob);
		downloadLink.onclick = function(e) { document.body.removeChild(event.target); };
		downloadLink.style.display = "none";
		document.body.appendChild(downloadLink);
	}

	downloadLink.click();
}

// Prepares for handling dropped files
document.addEventListener('dragover', function(e) {
    e.stopPropagation();
    e.preventDefault();
    e.dataTransfer.dropEffect = 'copy';
}, false);

// Examines dropped files and sets up loading applicable ones
document.addEventListener('drop', function(e) {
    e.stopPropagation();
    e.preventDefault();
    for (var i = 0; i < e.dataTransfer.files.length; i++) {
		var file = e.dataTransfer.files[i];
        if (file.name.indexOf('.yml') == -1) continue;
		var reader = new FileReader();
        if (file.name.indexOf('skills') == 0) {
            reader.onload = loadSkills;
        }
        else if (file.name.indexOf('classes') == 0) {
            reader.onload = loadClasses;
        }
        else {
			reader.onload = loadIndividual;
        }
        reader.readAsText(file);
    }
}, false);

// Loads an individual skill or class file
function loadIndividual(e) {
	var text = e.target.result;
    if (text.indexOf('global:') >= 0)
    {
        loadAttributes(e);
    }
	else if (text.indexOf('components:') >= 0 || (text.indexOf('group:') == -1 && text.indexOf('combo:') == -1 && text.indexOf('skills:') == -1))
	{
		loadSkills(e);
	}
	else 
	{
		loadClasses(e);
	}
}

// Loads attribute data from a file
// e - event details
function loadAttributes(e) {
    var text = e.target.result;
    document.activeElement.blur();
    var yaml = parseYAML(text);
    ATTRIBS = Object.keys(yaml);
    if (!skillsActive) {
        activeClass.update();
        activeClass.createFormHTML();
    }
    localStorage.setItem('attribs', ATTRIBS);
}

// Loads skill data from a file after it has been read
// e - event details
function loadSkills(e) {
    var text = e.target.result;
    document.activeElement.blur();
    loadSkillText(text);
}

// Loads skill data from a string
function loadSkillText(text) {
    
	// Load new skills
	var data = parseYAML(text);
	for (var key in data)
	{
		if (data[key] instanceof YAMLObject && key != 'loaded')
		{
			if (isSkillNameTaken(key))
			{
				getSkill(key).load(data[key]);
				if (getSkill(key) == activeSkill)
				{
					activeSkill.apply();
					showSkillPage('builder');
				}
			}
			else
			{
				addSkill(key).load(data[key]);
			}
		}
	}
}

// Loads class data from a file after it has been read
// e - event details
function loadClasses(e) {
    var text = e.target.result;
    document.activeElement.blur();
    loadClassText(text);
}
	
// Loads class data from a string
function loadClassText(text) {
    
	// Load new classes
	var data = parseYAML(text);
	for (var key in data)
	{
		if (data[key] instanceof YAMLObject && key != 'loaded' && !isClassNameTaken(key))
		{
			if (isClassNameTaken(key))
			{
				getClass(key).load(data[key]);
				if (getClass(key) == activeClass)
				{
					activeClass.createFormHTML();
				}
			}
			else
			{
				addClass(key).load(data[key]);
			}
		}
	}
}

/**
 * Loads a section of config data
 */
function loadSection(data) 
{	
	this.components = [];
	for (var x in data) {
		if (x == this.dataKey) {
			var attribs = data[x];
			for (var y in attribs) {
				for (var i = 0; i < this.data.length; i++) {
					if (this.data[i].key == y && this.data[i].load) {
						this.data[i].load(attribs[y]);
						break;
					} else if (this.data[i].key + '-base' == y && this.data[i].loadBase) {
						this.data[i].loadBase(attribs[y]);
						break;
					} else if (this.data[i].key + '-scale' == y && this.data[i].loadScale) {
						this.data[i].loadScale(attribs[y]);
						break;
					} else if (this.data[i].key == y && y === "mana-tick") {
						this.data[i].loadValue(attribs[y]);
					}
				}
			}
		} else if (x == this.componentKey) {
			var components = data[x];
			for (var y in components)
			{
				var type = components[y].type;
				var list;
				if (type == Type.TRIGGER)
				{
					list = Trigger;
				}
				else if (type == Type.TARGET)
				{
					list = Target;
				}
				else if (type == Type.CONDITION)
				{
					list = Condition;
				}
				else if (type == Type.MECHANIC)
				{	
					list = Mechanic;
				}
				
				var key = y;
				if (key.indexOf('-') > 0) key = key.substring(0, key.indexOf('-'));
				if (list !== undefined)
				{
					for (var z in list)
					{
						if (list[z].name.toLowerCase() == key.toLowerCase())
						{
							var component = new list[z].construct();
							component.parent = this;
							this.components.push(component);
							component.load(components[y]);
						}
					}
				}
			}
		}
		else if (this.dataKey != 'data')
		{
			for (var i = 0; i < this.data.length; i++)
			{
				if (this.data[i].key == x)
				{
					if (!this.data[i].load)
					{
						debugger;
					}
					this.data[i].load(data[x]);
					break;
				}
				else if (this.data[i].key + '-base' == x)
				{
					this.data[i].loadBase(data[x]);
					break;
				}
				else if (this.data[i].key + '-scale' == x)
				{
					this.data[i].loadScale(data[x]);
					break;
				}
			}
		}
	}
}

/**
 * Remember the current session data for next time
 */
window.onbeforeunload = function() {
    localStorage.setItem('skillData', getSkillSaveData());
    localStorage.setItem('classData', getClassSaveData());
    localStorage.setItem('skillsActive', this.skillsActive ? 'true' : 'false');
    localStorage.setItem('skillIndex', document.getElementById('skillList').selectedIndex);
    localStorage.setItem('classIndex', document.getElementById('classList').selectedIndex);
}