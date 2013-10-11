$(document).ready(function () {
	
	$(".fancybox").click(function() {
		$.fancybox({
				'padding'		: 0,
				'autoScale'		: false,
				'transitionIn'	: 'none',
				'transitionOut'	: 'none',
				'title'			: this.title,
				'width'		: 680,
				'height'		: 495,
				'href'			: this.href.replace(new RegExp("watch\\?v=", "i"), 'v/'),
				'type'			: 'swf',
				'swf'			: {
				   	 'wmode'		: 'transparent',
					'allowfullscreen'	: 'true'
				}
			});

		return false;
	});
	
	if (/stage1aDone/.test($("tbody").attr("class"))) {
		$(".stars span").unbind();
	} 
	$(".stars").each(function() {
		if (/(\d)stars/.test( $(this).parents("tr").attr("class"))) {
			var rating = parseInt($(this).parents("tr").attr("class").match(/(\d)stars/)[1]);
			for (var i=1; i<=rating; i++) {
				$(this).children().filter("."+i.toString()).addClass("clicked");
			}
		}
	})
	if (!/stage1aDone/.test($("tbody").attr("class"))) {
		$(".stars span").mouseenter( function() {
			$(this).addClass("hover");
			$(this).prevAll().addClass("hover");
		}).mouseleave( function() {
			$(this).removeClass("hover");
			$(this).prevAll().removeClass("hover");
		}).click( function() {
			$(this).parent().children().removeClass("clicked").removeClass("hover");
			$(this).addClass("clicked");
			$(this).prevAll().addClass("clicked");
			var movieId = $(this).parents("tr").attr("class").match(/movie-id-(\d+)/)[1];
			var rating = $(this).parent().children().filter(".clicked").last().attr("class").match(/\d+/)[0];
			$.ajax({
				type: "POST",
				url: "users/rateMovie",
				data: {
					"movieId" : movieId,
					"rating" : rating
				}
			});
		})
	}
});