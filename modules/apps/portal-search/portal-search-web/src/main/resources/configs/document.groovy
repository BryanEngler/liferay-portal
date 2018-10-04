import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.PortalUtil;

long repositoryId = GroupLocalServiceUtil.getFriendlyURLGroup(PortalUtil.getCompanyId(actionRequest), "/guest").getGroupId();
String title = "donut document";
String description = "chocolate donut";
ServiceContext serviceContext = ServiceContextFactory.getInstance(DLFileEntry.class.getName(), actionRequest);

DLAppServiceUtil.addFileEntry(repositoryId, 0, StringPool.BLANK, StringPool.BLANK, title, description, StringPool.BLANK, new byte[0], serviceContext);
