
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

Role role = RoleLocalServiceUtil.getRole(CompanyThreadLocal.getCompanyId(), RoleConstants.ADMINISTRATOR);

long userId = UserLocalServiceUtil.getRoleUsers(role.getRoleId(), 0, 1).get(0).getUserId();
long groupId = GroupLocalServiceUtil.getFriendlyURLGroup(PortalUtil.getCompanyId(actionRequest), "/guest").getGroupId();

Locale defaultLocale = LocaleUtil.fromLanguageId("en_US");
String title = "donut web content";
String content = "jelly donut";

Map<Locale, String> titleMap = new HashMap<Locale, String>();
titleMap.put(defaultLocale, title.toString());

Map<Locale, String> descriptionMap = new HashMap<Locale, String>();
descriptionMap.put(defaultLocale, title.toString());

StringBundler sb = new StringBundler(8);

sb.append("<?xml version=\"1.0\"?>");
sb.append("<root available-locales=\"en_US\" default-locale=\"en_US\">");
sb.append("<dynamic-element name=\"content\" type=\"text_area\" index-type=\"keyword\" index=\"0\" instance-id=\"ilvi\">");
sb.append("<dynamic-content language-id=\"en_US\"><![CDATA[");
sb.append(content);
sb.append("]]></dynamic-content>");
sb.append("</dynamic-element>");
sb.append("</root>");

User user = UserLocalServiceUtil.getUser(userId);

Calendar calendar = CalendarFactoryUtil.getCalendar(user.getTimeZone());

int displayDateMonth = calendar.get(Calendar.MONTH);
int displayDateDay = calendar.get(Calendar.DAY_OF_MONTH);
int displayDateYear = calendar.get(Calendar.YEAR);
int displayDateHour = calendar.get(Calendar.HOUR_OF_DAY);
int displayDateMinute = calendar.get(Calendar.MINUTE);

ServiceContext serviceContext = ServiceContextFactory.getInstance(JournalArticle.class.getName(), actionRequest);

serviceContext.setScopeGroupId(groupId);

JournalArticleLocalServiceUtil.addArticle(
	userId, groupId, 0, 0, 0, StringPool.BLANK, true, 1, titleMap, descriptionMap, sb.toString(), "BASIC-WEB-CONTENT",
	"BASIC-WEB-CONTENT", null, displayDateMonth, displayDateDay, displayDateYear, displayDateHour, displayDateMinute, 
	0, 0, 0, 0, 0, true, 0, 0, 0, 0, 0, true, true, false, null, null, null, null, serviceContext);
