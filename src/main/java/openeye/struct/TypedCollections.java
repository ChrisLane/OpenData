package openeye.struct;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import openeye.Log;
import openeye.protocol.reports.IReport;
import openeye.protocol.reports.ReportAnalytics;
import openeye.protocol.reports.ReportCrash;
import openeye.protocol.reports.ReportFileContents;
import openeye.protocol.reports.ReportFileInfo;
import openeye.protocol.reports.ReportKnownFiles;
import openeye.protocol.reports.ReportPing;
import openeye.protocol.responses.ResponseError;
import openeye.protocol.responses.ResponseFileContents;
import openeye.protocol.responses.ResponseFileInfo;
import openeye.protocol.responses.ResponseKnownCrash;
import openeye.protocol.responses.ResponseModMsg;
import openeye.protocol.responses.ResponsePong;
import openeye.protocol.responses.ResponseRemoveFile;
import openeye.protocol.responses.ResponseSuspend;
import openeye.responses.IExecutableResponse;
import openeye.responses.ResponseErrorAction;
import openeye.responses.ResponseFileContentsAction;
import openeye.responses.ResponseFileInfoAction;
import openeye.responses.ResponseKnownCrashAction;
import openeye.responses.ResponseModMsgAction;
import openeye.responses.ResponsePongAction;
import openeye.responses.ResponseRemoveFileAction;
import openeye.responses.ResponseSuspendAction;

public class TypedCollections {

	private abstract static class TypedListConverter<T> implements JsonSerializer<Collection<T>>, JsonDeserializer<Collection<T>> {
		private final BiMap<String, Class<? extends T>> mapping;

		private TypedListConverter(BiMap<String, Class<? extends T>> mapping) {
			this.mapping = mapping;
		}

		@Override
		public JsonElement serialize(Collection<T> src, Type typeOfSrc, JsonSerializationContext context) {
			JsonArray result = new JsonArray();

			for (T entry : src) {
				final Class<? extends Object> entryClass = entry.getClass();
				final String type = mapping.inverse().get(entryClass);
				if (type != null) {
					JsonObject serializedReport = context.serialize(entry).getAsJsonObject();
					serializedReport.addProperty("type", type);
					result.add(serializedReport);
				} else Log.warn("Trying to serialize class without mapping: %s", entryClass);
			}

			return result;
		}

		@Override
		public Collection<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonArray requests = json.getAsJsonArray();

			Collection<T> result = createCollection();
			for (JsonElement e : requests) {
				try {
					JsonObject obj = e.getAsJsonObject();
					JsonElement type = obj.get("type");
					String typeId = type.getAsString();

					Class<? extends T> cls = mapping.get(typeId);
					if (cls != null) {
						T request = context.deserialize(obj, cls);
						result.add(request);
					} else Log.warn("Invalid request type: %s", typeId);
				} catch (Throwable t) {
					Log.warn(t, "Failed to deserialize request %s", e);
				}
			}

			return result;
		}

		protected abstract Collection<T> createCollection();
	}

	public static class ReportsList extends ArrayList<IReport> {
		private static final long serialVersionUID = -6580030458427773185L;
	}

	public static class ResponseList extends ArrayList<IExecutableResponse> {
		private static final long serialVersionUID = 4069373518963113118L;
	}

	private static final BiMap<String, Class<? extends IReport>> REPORTS_TYPES = HashBiMap.create();
	private static final BiMap<String, Class<? extends IExecutableResponse>> RESPONSE_TYPES = HashBiMap.create();

	public static final Object REPORT_LIST_CONVERTER = new TypedListConverter<IReport>(REPORTS_TYPES) {
		@Override
		protected Collection<IReport> createCollection() {
			return new ReportsList();
		}
	};

	public static final Object RESPONSE_LIST_CONVERTER = new TypedListConverter<IExecutableResponse>(RESPONSE_TYPES) {
		@Override
		protected Collection<IExecutableResponse> createCollection() {
			return new ResponseList();
		}
	};

	static {
		REPORTS_TYPES.put(ReportAnalytics.TYPE, ReportAnalytics.class);
		REPORTS_TYPES.put(ReportFileInfo.TYPE, ReportFileInfo.class);
		REPORTS_TYPES.put(ReportCrash.TYPE, ReportCrash.class);
		REPORTS_TYPES.put(ReportPing.TYPE, ReportPing.class);
		REPORTS_TYPES.put(ReportKnownFiles.TYPE, ReportKnownFiles.class);
		REPORTS_TYPES.put(ReportFileContents.TYPE, ReportFileContents.class);

		RESPONSE_TYPES.put(ResponseFileInfo.TYPE, ResponseFileInfoAction.class);
		RESPONSE_TYPES.put(ResponsePong.TYPE, ResponsePongAction.class);
		RESPONSE_TYPES.put(ResponseFileContents.TYPE, ResponseFileContentsAction.class);
		RESPONSE_TYPES.put(ResponseRemoveFile.TYPE, ResponseRemoveFileAction.class);
		RESPONSE_TYPES.put(ResponseModMsg.TYPE, ResponseModMsgAction.class);
		RESPONSE_TYPES.put(ResponseError.TYPE, ResponseErrorAction.class);
		RESPONSE_TYPES.put(ResponseKnownCrash.TYPE, ResponseKnownCrashAction.class);
		RESPONSE_TYPES.put(ResponseSuspend.TYPE, ResponseSuspendAction.class);
	}

}
