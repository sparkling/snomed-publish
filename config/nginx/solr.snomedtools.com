server {

        listen 80;
        server_name solr.snomedtools.com;
        root /opt/solr/example/solr-webapp/webapp;
        rewrite ^/solr(.*)$ $1 last;
        location / {
            proxy_set_header X-Forwarded-Host $host;
            proxy_set_header X-Forwarded-Server $host;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_pass http://127.0.0.1:8983/solr/;
            proxy_redirect http://127.0.0.1:8983/solr/ /;
        }
        
        #index index.html index.htm;

        # Make site accessible from http://localhost/
        #server_name localhost;
}
