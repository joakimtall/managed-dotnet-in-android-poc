using System.Runtime.InteropServices;
using System.Text;
using System.Diagnostics.CodeAnalysis;
using System.Text.Json;
using System.Text.Json.Serialization;
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

[JsonSerializable(typeof(LineItemData))]
[JsonSourceGenerationOptions(PropertyNamingPolicy = JsonKnownNamingPolicy.CamelCase, WriteIndented = true)]
internal partial class LineItemJsonContext : JsonSerializerContext
{
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
        try
        {
            var lineItem = new LineItemData
            {
                Name = "3 pack socks",
                LineItemType = LineItemType.Sale,
                Amount = 99.99m
            };
            string json = JsonSerializer.Serialize(lineItem, LineItemJsonContext.Default.LineItemData);
            byte[] utf8Bytes = Encoding.UTF8.GetBytes(json + '\0');
            IntPtr buffer = Marshal.AllocHGlobal(utf8Bytes.Length);
            Marshal.Copy(utf8Bytes, 0, buffer, utf8Bytes.Length);
            return buffer;
        }
        catch
        {
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
}
