require 'rake'
require 'net/http'
require 'uri'

desc "Steal tests from the public suffix page"
task :steal_tests do
  file_uri = URI  "http://publicsuffix.org/list/test.txt"
  
  txt = Net::HTTP.get_response(file_uri).body

  txt.lines.map(&:strip).reject(&:empty?).each do |l|
    if l.start_with?("#")
      puts l.gsub!("#", ";;")
      next
    end
    
    test_host, asserted_domain = l.match(/^checkPublicSuffix\((.+), (.+)\);/).values_at(1,2) rescue puts ";;Error processing line: '#{l}'"

    [test_host, asserted_domain].each {|s| s.gsub!("'", '"')}
    [test_host, asserted_domain].each {|s| s.gsub!("NULL", "nil")}

    
    puts "(is (= #{asserted_domain} (:domain (parse #{test_host}))))"
  end
end
