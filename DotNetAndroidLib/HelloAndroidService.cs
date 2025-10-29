using System.Runtime.InteropServices;
using System.Text;
using System.Diagnostics.CodeAnalysis;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Text.Json.Serialization.Metadata;
using HandlebarsDotNet;

namespace DotNetAndroidLib;

[JsonConverter(typeof(JsonStringEnumConverter<LineItemType>))]
public enum LineItemType
{
    Sale,
    Resell,
    OtherSale,
    ShoppingBag,
    GiftCard,
    ShippingFee
}

public class LineItemData
{
    public string Name { get; set; } = "";
    public LineItemType LineItemType { get; set; }
    public decimal Amount { get; set; }
}

public static class HelloAndroidService
{
    [UnmanagedCallersOnly(EntryPoint = "create_hello")]
    [DynamicDependency(DynamicallyAccessedMemberTypes.PublicProperties, typeof(TemplateData))]
    public static unsafe IntPtr CreateHello()
    {
        try
        {
            var template = Handlebars.Compile("🎉 Handlebars says: Hello from {{runtime}} on {{platform}}!");
            var data = new TemplateData
            {
                runtime = ".NET 9 NativeAOT",
                platform = "Android"
            };
            string result = template(data);
            byte[] utf8Bytes = Encoding.UTF8.GetBytes(result + '\0');
            IntPtr buffer = Marshal.AllocHGlobal(utf8Bytes.Length);
            Marshal.Copy(utf8Bytes, 0, buffer, utf8Bytes.Length);
            return buffer;
        }
        catch
        {
            return IntPtr.Zero;
        }
    }

    private class TemplateData
    {
        public string runtime { get; set; } = "";
        public string platform { get; set; } = "";
    }

    [UnmanagedCallersOnly(EntryPoint = "get_line_item_json")]
    public static unsafe IntPtr GetLineItemJson()
    {
        try {
            var lineItem = new LineItemData
            {
                Name = "3 pack socks",
                LineItemType = LineItemType.Sale,
                Amount = 99.99m
            };
            var opts = new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
                WriteIndented = true,
                TypeInfoResolver = new DefaultJsonTypeInfoResolver()
            };

            // AOT-safe: generic overload, T is known (LineItemData)
            string json = JsonSerializer.Serialize(lineItem, opts);
            byte[] utf8Bytes = Encoding.UTF8.GetBytes(json + '\0');
            IntPtr buffer = Marshal.AllocHGlobal(utf8Bytes.Length);
            Marshal.Copy(utf8Bytes, 0, buffer, utf8Bytes.Length);
            return buffer;
        }
        catch (Exception ex)
        {
            AndroidLog.E("DotNetAndroidLib", $"get_line_item_json failed: {ex}");
            return IntPtr.Zero;
        }
    }

    [UnmanagedCallersOnly(EntryPoint = "free_string")]
    public static unsafe void FreeString(IntPtr ptr)
    {
        if (ptr != IntPtr.Zero)
        {
            Marshal.FreeHGlobal(ptr);
        }
    }

    internal static class AndroidLog
    {
        private const int ANDROID_LOG_UNKNOWN = 0;
        private const int ANDROID_LOG_DEFAULT = 1;
        private const int ANDROID_LOG_VERBOSE = 2;
        private const int ANDROID_LOG_DEBUG = 3;
        private const int ANDROID_LOG_INFO = 4;
        private const int ANDROID_LOG_WARN = 5;
        private const int ANDROID_LOG_ERROR = 6;
        private const int ANDROID_LOG_FATAL = 7;

        [System.Runtime.InteropServices.DllImport("log")]
        private static extern int __android_log_write(int prio, string tag, string text);

        public static void E(string tag, string message) => __android_log_write(ANDROID_LOG_ERROR, tag, message);
        public static void W(string tag, string message) => __android_log_write(ANDROID_LOG_WARN, tag, message);
        public static void I(string tag, string message) => __android_log_write(ANDROID_LOG_INFO, tag, message);
        public static void D(string tag, string message) => __android_log_write(ANDROID_LOG_DEBUG, tag, message);
        public static void V(string tag, string message) => __android_log_write(ANDROID_LOG_VERBOSE, tag, message);
    }

}
