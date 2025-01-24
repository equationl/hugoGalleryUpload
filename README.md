
# Hugo Gallery Uploader

Hugo 画廊图片上传工具。

基于华为的 OBS 云存储。

# 特性

本地读取图片 Exif 信息后上传至 OBS，然后生成对应的 Hugo 短代码。

[效果演示](https://www.likehide.com/photograph/2025_01_01/)

# 注意

需要配合对应的 Hugo 短代码使用，因为代码很简单，就不另外开一个仓库了，直接在下面附上：

<details>
<summary>点击展开</summary>

```html
{{ $galleryId := (printf "gallery-%v-%v" .Page.File.UniqueID .Ordinal)}}
{{ $galleryWrapperId := (printf "gallery-%v-%v-wrapper" .Page.File.UniqueID .Ordinal)}}

{{$desList := split .Params.imgDes ","}}
{{$titleList := split .Params.imgTitle ","}}
{{$imgWidthList := split .Params.imgWidth ","}}
{{$imgHeightList := split .Params.imgHeight ","}}

{{ $swipeboxParameters := .Get "swipeboxParameters" | default (.Site.Params.gallerySwipeboxParameters | default "") }}

{{ $rowHeight := .Get "rowHeight" | default (.Site.Params.galleryRowHeight | default 150) }}

{{ $margins := .Get "margins" | default (.Site.Params.galleryRowMargins | default 5) }}

{{ $randomize := .Get "randomize" | default (.Site.Params.galleryRandomize | default false) }}


{{ $filterOptions := .Get "filterOptions" | default (.Site.Params.galleryFilterOptions | default "[]") }}
{{ if not $filterOptions }}
	{{ $filterOptions = "[]" }}
{{ end }}

{{ $justifiedGalleryParameters := .Get "justifiedGalleryParameters" | default (.Site.Params.galleryJustifiedGalleryParameters | default "") }}

{{ $storeSelectedFilterInUrl := .Get "storeSelectedFilterInUrl" | default (.Site.Params.storeSelectedFilterInUrl | default false) }}

<script src="{{ "shortcode-gallery/jquery-3.7.1.min.js" | relURL }}"></script>
<script src="{{ "shortcode-gallery/lazy/jquery.lazy.min.js" | relURL }}"></script>
<script src="{{ "shortcode-gallery/swipebox/js/jquery.swipebox.min.js" | relURL }}"></script>
<link rel="stylesheet" href="{{ "shortcode-gallery/swipebox/css/swipebox.min.css"| relURL }}">
<script src="{{ "shortcode-gallery/justified_gallery/jquery.justifiedGallery.min.js"| relURL }}"></script>
<link rel="stylesheet" href="{{ "shortcode-gallery/justified_gallery/justifiedGallery.min.css"| relURL }}"/>

<style>
    .jg-entry img {
      transition: transform .25s ease-in-out !important;
    }

    .jg-entry img:hover {
      transform: scale(1.1);
    }
</style>


<div id="{{ $galleryWrapperId }}" class="gallery-wrapper">
<div id="{{ $galleryId }}" class="justified-gallery">

	{{ range $i, $imgPath := split .Params.imgPath "," }}

	<div>
		<a 	href="{{ $imgPath }}" 
			class="galleryImg"
			title="{{ index $titleList $i }}"
			data-description="{{index $desList $i}}"
		>
		<img
			width="{{ index $imgWidthList $i}}" height="{{ index $imgHeightList $i }}"
			src="{{ $imgPath }}?x-image-process=style/thumbnail_blur"
			class="lazy"
			data-src="{{ $imgPath }}"
			alt="{{ index $desList $i }}"
		>
		</a>
	</div>


	{{ end }}
</div>
</div>


<script>
	if (!("HSCGjQuery" in window)) {
		if (!window.jQuery) {
			throw new Error("jQuery is not loaded, hugo-shortcode-gallery wont work without it!");
		}
		window.HSCGjQuery = window.jQuery.noConflict(true);
	};

	// Isolate all our variables from other scripts.
	// See: https://www.nicoespeon.com/en/2013/05/properly-isolate-variables-in-javascript/
	// We also expect to get jQuery as a parameter, so that if a second instance of jQuery is loaded
	// after this script is executed (e.g. by a Hugo theme), we will still be referencing the previous
	// version of jQuery that has all our plugins loaded.
	(function($) {

		$( document ).ready(() => {
			const gallery = $("#{{ $galleryId }}");
			{{ $lastRowJustification := "justify" }}

			// the instance of swipebox, it will be set once justifiedGallery is initialized
			let swipeboxInstance = null;

			// before the gallery initialization the listener has to be added
			// else we can get a race condition and the listener is never called
			gallery.on('jg.complete', () => {
				// if there is already some low resolution image data loaded, then we will wait for loading´
				// the hi-res until the justified gallery has done the layout
				$(() => {
					$('.lazy').Lazy({
						visibleOnly: true,
						afterLoad: element => element.css({filter: "none", transition: "filter 1.0s ease-in-out"})
					});
				});


				swipeboxInstance = $('.galleryImg').swipebox(
					$.extend({},
						{ {{ $swipeboxParameters | safeJS }} }
					)
				);
			});

			// initialize the justified gallery
			gallery.justifiedGallery($.extend(
				{
					rowHeight : {{ $rowHeight }},
					margins : {{ $margins }},
					border : 0,
					randomize : {{ $randomize }},
					waitThumbnailsLoad : false,
					lastRow : {{ $lastRowJustification }},
					captions : false,
					// if there is at least one filter option
					{{ if not (eq $filterOptions "[]") }}
						// we first show no images at all
						// till the code way below selects a filter and applies it
						// this prevent creating the layout twice
						filter : () => false
					{{ end }}
				},
				{ {{ $justifiedGalleryParameters | safeJS }} }
			));

			// only include JS code for filter options if there at least one filter option
			{{ if not (eq $filterOptions "[]") }}

				// this function can be used to create a function that can be used by justifiedGallery
				// for filtering images by their metadata
				function createMetadataFilter(filterFunction) {
					return (entry, index, array) => {
						let meta = $(entry).find("a").attr("data-meta");
						meta = meta ? JSON.parse(meta) : {};

						let include = filterFunction(meta);

						// only those images visible in justified gallery should be displayed
						// in swipebox (only <a> with class galleryImg are displayed in swipebox)
						$(entry).find("a").toggleClass("galleryImg", include);

						return include;
					}
				}

				// this function returns a function that can be used by justifiedGallery
				// for filtering images by their tags
				function createTagFilter(tagsRegexString) {
					const tagsRegex = RegExp(tagsRegexString);
					return createMetadataFilter(meta => {
						let tags = meta.Tags;
						tags = tags ? tags : [];
						return tags.some(tag => tagsRegex.test(tag));
					});
				};

				// this function returns a function that can be used by justifiedGallery
				// for filtering images by their description
				function createImageDescriptionFilter(descriptionRegexString) {
					const descriptionRegex = RegExp(descriptionRegexString);
					return createMetadataFilter(meta => {
						let imageDescription = meta.ImageDescription;
						return imageDescription !== null && descriptionRegex.test(imageDescription);
					});
				};

				// this function returns a function that can be used by justifiedGallery
				// for filtering images by their star rating
				function createRatingFilter(min, max) {
					return createMetadataFilter(meta => {
						let rating = meta.Rating;
						if(rating === null){
							rating = -1;
						}
						return rating >= min && rating <= max;
					});
				};

				// this function returns a function that can be used by justifiedGallery
				// for filtering images by their color labels
				function createColorLabelFilter(color) {
					color = color.charAt(0).toLowerCase()
					return createMetadataFilter(meta => {
						let colors = meta.ColorLabels;
						return colors && colors.includes(color);
					});
				};


				const filterOptions = {{ $filterOptions | safeJS }};

				// insert a div for inserting filter buttons before the gallery
				const filterBar = $("<div class='justified-gallery-filterbar'/>");
				gallery.before(filterBar);

				var wrapper = document.getElementById("{{ $galleryWrapperId }}");

				// inline svg icons
				const expandIcon = '{{ (resources.Get "shortcode-gallery/font-awesome/expand-alt-solid.svg").Content | safeHTML }}';
				const compressIcon = '{{ (resources.Get "shortcode-gallery/font-awesome/compress-alt-solid.svg").Content | safeHTML }}';

				function setFulltab(activate) {
					if(activate == wrapper.classList.contains("fulltab")){
						return;  // nothing to do, we are already in the right state
					}

					wrapper.classList.toggle("fulltab");
					gallery.justifiedGallery({
						rowHeight : {{ $rowHeight }} * (activate ? 1.5 : 1.0),
						lastRow: (activate ? "nojustify": {{ $lastRowJustification }}),
						// force justifiedGallery to refresh
						refreshTime: 0,
					});
					// force justifiedGallery to refresh
					gallery.data('jg.controller').startImgAnalyzer();
					fullTabButton.html(wrapper.classList.contains("fulltab") ? compressIcon : expandIcon)
				};

				const fullTabButton = $("<button/>");
				fullTabButton.html(expandIcon);
				fullTabButton.click(() => setFulltab(!wrapper.classList.contains("fulltab")));
				filterBar.append(fullTabButton);
				$(document).keyup(e => {
					// when ESC is pressed
					if (e.keyCode === 27){
						setFulltab(false);
					}
				});

				function activateFilterButton(filterButton) {
					// activate associated filter
					gallery.justifiedGallery({filter : filterButton.filter});
					// remove select class from all other selected buttons
					filterBar.find('.selected').removeClass('selected');
					filterButton.addClass("selected");
				};

				// check if the url contains an instruction to apply a specific filter
				// eg. example.com/images/#gallery-filter=Birds
				const params = new URLSearchParams(location.hash.replace(/^\#/,""));
				let activeFilter = params.get('gallery-filter');
				if (!activeFilter) {
					// default to first filter
					activeFilter = filterOptions[0].label;
				}

				// create a button for each filter entry
				filterOptions.forEach(filterConfig => {
					let filter;  // create a filter function based on the available attributes of filterConfig
					if(filterConfig.tags) {
						filter = createTagFilter(filterConfig.tags);
					} else if(filterConfig.rating){
						if(filterConfig.rating.includes("-")){
							minMax = filterConfig.rating.split("-");  // e.g. "3-5"
						} else {
							minMax = [filterConfig.rating, filterConfig.rating];  // e.g. "4"
						}
						filter = createRatingFilter(parseInt(minMax[0]), parseInt(minMax[1]));
					} else if(filterConfig.color_label){
						filter = createColorLabelFilter(filterConfig.color_label);
					} else if(filterConfig.description){
						filter = createImageDescriptionFilter(filterConfig.description);
					} else {
						// default to always true filter
						filter = createMetadataFilter(meta => true);
					}

					const filterButton = $("<button/>");
					filterButton.text(filterConfig.label);
					filterButton.filter = filter;
					filterButton.click(() => {
						activateFilterButton(filterButton);

						{{ if $storeSelectedFilterInUrl }}
							// save applied filter in browser url
							const params = new URLSearchParams(location.hash.replace(/^\#/,""));
							params.set('gallery-filter', filterConfig.label);+
							window.history.replaceState("", "", location.pathname + location.search + "#" + params.toString());
						{{ end }}
					});
					filterBar.append(filterButton);

					if(filterConfig.label.toLowerCase() === activeFilter.toLowerCase()) {
						activateFilterButton(filterButton);
					}
				});
			{{ end }}
		});

	// End of our variable-isolating and self-executing anonymous function.
	// We call it with the one version of jQuery that was used to load our plugins.
	// See: http://blog.nemikor.com/2009/10/03/using-multiple-versions-of-jquery/
	})(window.HSCGjQuery)
</script>
```

</details>

以上代码基于 [hugo-shortcode-gallery](https://github.com/mfg92/hugo-shortcode-gallery) 项目修改。

未提供的样式文件、js文件、图片资源等也烦请从原始项目中获取。

## 使用方法

直接在 md 文件中写入由本程序生成的代码即可。

样例：

```markdown
---
title: "文章标题"
date: 2025-01-01T11:37:14+08:00
---

文章内容，下面是图片代码：


{{<gallery
        imgPath="http://obs.test.com/1.jpg,http://obs.test.com/2.jpg,http://obs.test.com/3.jpg"
        imgDes="SONY ILCE-7CM2 + E 50-400mm F4.5-6.3 A067 <br> 400mm f/6.3 1/1600sec ISO500<br>2024-12-27 17:47:47,SONY ILCE-7CM2 + Sony FE 24-70mm F2.8 GM II (SEL2470GM2) <br> 70mm f/2.8 0.1sec ISO2500<br>2024-12-27 18:54:10,SONY ILCE-7CM2 + Sony FE 24-70mm F2.8 GM II (SEL2470GM2) <br> 70mm f/2.8 1/8sec ISO2500<br>2024-12-27 18:54:31"
        imgTitle="DSC09974.jpg,DSC09977.jpg,DSC09980.jpg"
        imgWidth="199,450,450"
        imgHeight="300,300,300">}}
```

短代码必须参数为：

- `imgPath` 图片地址
- `imgDes` 图片描述
- `imgTitle` 图片标题
- `imgWidth` 图片显示宽度
- `imgHeight` 图片显示高度

以上参数必须是逗号分隔的字符串，每个逗号之间表示一个数据。

每个参数提供的数据数量必须一致。

其他参数非必须，具体有什么参数请自己看代码。🙃

建议不要自己手搓嗷，容易写错啊🤡，~~而且你手搓了，我写这个客户端还有啥意义~~
